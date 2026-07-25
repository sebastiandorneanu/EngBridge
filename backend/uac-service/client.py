import grpc
from protos import auth_pb2, auth_pb2_grpc
from models.user import UserRole
def run():
    channel = grpc.insecure_channel("localhost:50051")
    stub = auth_pb2_grpc.AuthServiceStub(channel)
   
    print("Logging in as admin...")
    login_response = stub.Login(auth_pb2.LoginRequest(
        username="admin",
        password="admin1234"
    ))
    
    if login_response.error:
        print("Login failed:", login_response.error)
        return
    
    token = login_response.token 

    metadata = [('authorization', f'Bearer {token}')]  
    print("Response:")
    print(token)
    response = stub.Register(
        auth_pb2.RegisterRequest(
            username="newuser",
            password="secret",
            email="newuser@example.com",
            role=UserRole.OWNER.value 
        ),
        metadata=metadata
    )

    print("Response:")
    print(response)

if __name__ == "__main__":
    run()
