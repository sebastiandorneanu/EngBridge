
from sqlalchemy import Column,Integer,String,Boolean,Enum
from config.database import Base
import enum
class UserRole(enum.Enum):
    ADMIN = "ADMIN"
    STUDENT = "STUDENT"

class User(Base):
    __tablename__ = "users"
    
    uid = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), unique=True, nullable=False, index=True)
    email = Column(String(100), unique=True, nullable=False, index=True)
    password= Column(String(255), nullable=False)
    role = Column(Enum(UserRole), nullable=False)  

