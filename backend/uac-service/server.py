import grpc
from concurrent import futures
from  protos.auth_pb2 import *
import protos.auth_pb2_grpc
from sqlalchemy.orm import Session
from config.database import SessionLocal
from models import User    
from models.user import UserRole
import bcrypt
import datetime
import uuid
import jwt
from jwt import ExpiredSignatureError, InvalidTokenError
from config.database import settings
from config.database import init_db


print("SERVER SETTINGS DB URL:", settings.database_url)
print("MYSQL_USER:", settings.mysql_user)
print("MYSQL_PASSWORD:", settings.mysql_password)
print("Database URL:", settings.database_url)

SECRET_KEY = settings.secret_key
ALGORITHM = settings.algorithm
SERVICE_URL=settings.service_url
class AuthService(protos.auth_pb2_grpc.AuthServiceServicer):
    
    def __init__(self):
        self.blacklist = set() 
    
    def Register(self, request, context):
     db: Session = SessionLocal()
     try:
        
         exist = db.query(User).filter(User.username == request.username).first()
         if exist:
             return protos.auth_pb2.RegisterResponse(
                 success=False,
                 message="Username already exists."
             )

         hashed_password = bcrypt.hashpw(
             request.password.encode('utf-8'),
             bcrypt.gensalt()
         ).decode('utf-8')

         new_user = User(
             username=request.username,
             email=request.email,
             password=hashed_password,
             role=UserRole.STUDENT  
         )

         db.add(new_user)
         db.commit()
         db.refresh(new_user)
         
        
         return protos.auth_pb2.RegisterResponse(
                success=True,
                message=f"User {request.username} registered successfully."
            )

     except Exception as e:
         db.rollback()
         return protos.auth_pb2.RegisterResponse(
             success=False,
             message=f"Error: {str(e)}"
         ) 
     finally:
         db.close()

    def CreateUser(self,request,context):
        db: Session = SessionLocal()
        try:
            md = dict(context.invocation_metadata())
            auth_header = md.get('authorization', '')

            if not auth_header.startswith('Bearer '):
                context.abort(grpc.StatusCode.UNAUTHENTICATED, "Missing token")

            token_str = auth_header.split(' ')[1]

            try:
                payload = jwt.decode(token_str, SECRET_KEY, algorithms=[ALGORITHM])
            except ExpiredSignatureError:
                context.abort(grpc.StatusCode.UNAUTHENTICATED, "Token expired")
            except InvalidTokenError:
                context.abort(grpc.StatusCode.UNAUTHENTICATED, "Invalid token")

            role = payload.get("role", "")
            if role != "ADMIN":
                context.abort(grpc.StatusCode.PERMISSION_DENIED, "Only admin can create users")
            
            exist = db.query(User).filter(User.username == request.username).first()
            if exist:
                return protos.auth_pb2.CreateUserResponse(
                    success=False,
                    message="",
                    uid=0,
                    error="Username already exists."
                )

            hashed_password = bcrypt.hashpw(
                request.password.encode('utf-8'),
                bcrypt.gensalt()
            ).decode('utf-8')

            new_user = User(
                username=request.username,
                email=request.email,
                password=hashed_password,
                role=request.role  
            )

            db.add(new_user)
            db.commit()
            db.refresh(new_user)
            
            
            return protos.auth_pb2.CreateUserResponse(
                success=True,
                message=f"User {request.username} created successfully.",
                uid=new_user.uid,
            )
        except Exception as e:
                db.rollback()
                return protos.auth_pb2.CreateUserResponse(
                success=False,
                message="",
                uid=0,
                error=f"Error: {str(e)}"
            ) 
        finally:
                db.close()
  
    def Login(self, request, context):
        db: Session=SessionLocal()
        try:    
            print("Login attempt:", request.username)
            exist = db.query(User).filter(User.username == request.username).first()
            print("Found user:", exist)
            if not exist:
                return protos.auth_pb2.LoginResponse(
                    token="",
                    error="Invalid username or password."
                )
            if not bcrypt.checkpw(request.password.encode('utf-8'), exist.password.encode('utf-8')):
                return protos.auth_pb2.LoginResponse(
                    token="",
                    error="Invalid username or password."
                )
            header={
                "alg": ALGORITHM,
                "typ": "JWT"
                }
            from datetime import datetime, timedelta, timezone

            exp_time = datetime.now(timezone.utc) + timedelta(hours=1)
            payload = {
                "iss": SERVICE_URL,             
                "sub": str(exist.uid),            
                "exp": int(exp_time.timestamp()),   
                "jti": str(uuid.uuid4()),       
                "role": exist.role.value if hasattr(exist.role, 'value') else str(exist.role)
                }

            token = jwt.encode(headers=header, payload=payload, key=SECRET_KEY, algorithm=ALGORITHM)

            return protos.auth_pb2.LoginResponse(
                token=token,
                error=""
            )
        finally:
            db.close()

    def ValidateToken(self, request, context):
        token_str = request.token

        if token_str in self.blacklist:
            return protos.auth_pb2.ValidateResponse(
                valid=False, 
                userId="",
                role="",
                error="Token is blacklisted"
            )

        try:
            payload = jwt.decode(token_str, SECRET_KEY, algorithms=[ALGORITHM])

            return protos.auth_pb2.ValidateResponse(
                valid=True,
                userId=payload.get("sub", ""),
                role=str(payload.get("role", "")),
            )
        except ExpiredSignatureError:
            self.blacklist.add(token_str)
            return protos.auth_pb2.ValidateResponse(
                valid=False, 
                userId="",
                role="",
                error="Token has expired"
            )
        except InvalidTokenError:
            self.blacklist.add(token_str)
            return protos.auth_pb2.ValidateResponse(
                valid=False, 
                userId="",
                role="",
                error="Invalid token"
            )

    def InvalidateToken(self, request, context):
        token_str = request.token
        self.blacklist.add(token_str)

        return protos.auth_pb2.InvalidateResponse(
            success=True,
            message="Token invalidated successfully",
        )

    def Update(self,request,context):
        db:Session=SessionLocal()
        try:
            md = dict(context.invocation_metadata())
            auth_header = md.get('authorization', '')

            if not auth_header.startswith('Bearer '):
                context.abort(grpc.StatusCode.UNAUTHENTICATED, "Missing token")

            token_str = auth_header.split(' ')[1]

            try:
                payload = jwt.decode(token_str, SECRET_KEY, algorithms=[ALGORITHM])
            except ExpiredSignatureError:
                context.abort(grpc.StatusCode.UNAUTHENTICATED, "Token expired")
            except InvalidTokenError:
                context.abort(grpc.StatusCode.UNAUTHENTICATED, "Invalid token")

            current_user_id = int(payload.get("sub", "0"))
            role = payload.get("role", "")
            
            if not request.uid or request.uid <= 0:
                            return protos.auth_pb2.UpdateResponse(
                                success=False,
                                message="",
                                error="Invalid user ID"
                            )

            if role != "ADMIN" and current_user_id != request.uid:
                context.abort(grpc.StatusCode.PERMISSION_DENIED, "Only admin can update users")

            user = db.query(User).filter(User.uid == request.uid).first()
            if not user:
                return protos.auth_pb2.UpdateResponse(
                    success=False,
                    message="",
                    error="User not found"
                )

            if request.username:
                existing = db.query(User).filter(
                    User.username == request.username,
                    User.uid != request.uid
                ).first()

                if existing:
                    return protos.auth_pb2.UpdateResponse(
                        success=False,
                        message="",
                        error="Username already exists"
                    )
                
                user.username = request.username


            if request.password:
                hashed_password = bcrypt.hashpw(
                    request.password.encode('utf-8'),
                    bcrypt.gensalt()
                ).decode('utf-8')
                user.password = hashed_password
            
            if request.role:
                if role != "ADMIN":
                    return protos.auth_pb2.UpdateResponse(
                        success=False,
                        message="",
                        error="Only admin can change user roles"
                    )
                user.role = request.role

            db.commit()

            return protos.auth_pb2.UpdateResponse(
                success=True,
                message=f"User {user.username} updated successfully",
            )
        except Exception as e:
            db.rollback()
            return protos.auth_pb2.UpdateResponse(
                success=False,
                message="",
                error=f"Error: {str(e)}"
            )
        finally:
            db.close()

    def Delete(self,request,context):
        db:Session=SessionLocal()
        try:
            md = dict(context.invocation_metadata())
            auth_header = md.get('authorization', '')

            if not auth_header.startswith('Bearer '):
                context.abort(grpc.StatusCode.UNAUTHENTICATED, "Missing token")

            token_str = auth_header.split(' ')[1]

            try:
                payload = jwt.decode(token_str, SECRET_KEY, algorithms=[ALGORITHM])
            except ExpiredSignatureError:
                context.abort(grpc.StatusCode.UNAUTHENTICATED, "Token expired")
            except InvalidTokenError:
                context.abort(grpc.StatusCode.UNAUTHENTICATED, "Invalid token")

            current_user_id = int(payload.get("sub", "0"))
            role = payload.get("role", "")

            if not request.uid or request.uid <= 0:
                return protos.auth_pb2.UpdateResponse(
                    success=False,
                    message="",
                    error="Invalid user ID"
                )

            if role != "ADMIN" and current_user_id != request.uid:
                context.abort(grpc.StatusCode.PERMISSION_DENIED, "You can only delete your own account")

            user = db.query(User).filter(User.uid == request.uid).first()
            if not user:
                return protos.auth_pb2.DeleteResponse(
                    success=False,
                    message="",
                    error="User not found"
                )

            username = user.username
            db.delete(user)
            db.commit()

            return protos.auth_pb2.DeleteResponse(
                success=True,
                message=f"User {username} deleted successfully",
            )

        except Exception as e:
            db.rollback()
            return protos.auth_pb2.DeleteResponse(
                success=False,
                message="",
                error=f"Error: {str(e)}"
            )
        finally:
            db.close()

    def getAllUsers(self, request, context):
            db: Session = SessionLocal()
            try:
                md = dict(context.invocation_metadata())
                auth_header = md.get('authorization', '')

                if not auth_header.startswith('Bearer '):
                    context.abort(grpc.StatusCode.UNAUTHENTICATED, "Missing token")

                token_str = auth_header.split(' ')[1]

                try:
                    payload = jwt.decode(token_str, SECRET_KEY, algorithms=[ALGORITHM])
                except ExpiredSignatureError:
                    context.abort(grpc.StatusCode.UNAUTHENTICATED, "Token expired")
                except InvalidTokenError:
                    context.abort(grpc.StatusCode.UNAUTHENTICATED, "Invalid token")

                role = payload.get("role", "")
                if role != "ADMIN":
                    context.abort(grpc.StatusCode.PERMISSION_DENIED, "Only admin can view all users")

                users = db.query(User).all()
                
                user_list = []
                for user in users:
                    user_info = protos.auth_pb2.UserInfo(
                        uid=user.uid,
                        username=user.username,
                        email=user.email,
                        role=user.role.value if isinstance(user.role, UserRole) else user.role
                    )
                    user_list.append(user_info)

                return protos.auth_pb2.UserList(
                    users=user_list,
                    error=""
                )

            except Exception as e:
                return protos.auth_pb2.UserList(
                    users=[],
                    error=f"Error: {str(e)}"
                )
            finally:
                db.close()

    def getUser(self, request, context):
        db: Session = SessionLocal()
        try:
            md = dict(context.invocation_metadata())
            auth_header = md.get('authorization', '')

            if not auth_header.startswith('Bearer '):
                context.abort(grpc.StatusCode.UNAUTHENTICATED, "Missing token")

            token_str = auth_header.split(' ')[1]

            try:
                payload = jwt.decode(token_str, SECRET_KEY, algorithms=[ALGORITHM])
            except ExpiredSignatureError:
                context.abort(grpc.StatusCode.UNAUTHENTICATED, "Token expired")
            except InvalidTokenError:
                context.abort(grpc.StatusCode.UNAUTHENTICATED, "Invalid token")

            current_user_id = int(payload.get("sub", "0"))
            role = payload.get("role", "")

            if role != "ADMIN" and current_user_id != request.uid:
                context.abort(grpc.StatusCode.PERMISSION_DENIED, "You can only view your own account")

            user = db.query(User).filter(User.uid == request.uid).first()
            if not user:
                return protos.auth_pb2.UserResponse(
                    uid=0,
                    username="",
                    email="",
                    role="",
                    error="User not found"
                )

            return protos.auth_pb2.UserResponse(
                uid=user.uid,
                username=user.username,
                email=user.email,
                role=user.role.value if isinstance(user.role, UserRole) else user.role,
            )

        except Exception as e:
            return protos.auth_pb2.UserResponse(
                uid=0,
                username="",
                email="",
                role="",
                error=f"Error: {str(e)}"
            )
        finally:
            db.close()


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    protos.auth_pb2_grpc.add_AuthServiceServicer_to_server(AuthService(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    print("Server started on port 50051")
    server.wait_for_termination()

if __name__ == "__main__":
    
    init_db()
    
    serve()
