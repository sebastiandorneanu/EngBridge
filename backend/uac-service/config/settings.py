from pydantic_settings import BaseSettings
from functools import lru_cache
import os

print("Current working dir:", os.getcwd())
print("Settings file location:", os.path.dirname(__file__))
print("Looking for .env at:", os.path.join(os.path.dirname(__file__), ".env"))

class Settings(BaseSettings):
    mysql_host: str = "localhost"
    mysql_port: int = 3306
    mysql_database: str = "uac_db"
    mysql_user: str = "root"
    mysql_password: str = "root"

    grpc_port: int = 50051

    secret_key: str = "ksugaughy345y786y3ru98&&GJYR#$#@HDFKJSHDFKJ"
    algorithm: str = "HS256"

    service_name: str = "uac-service"
    service_url: str = "http://localhost:50051"
    
    class Config:
        env_file = os.path.join(os.path.dirname(__file__), ".env")
        case_sensitive = False
        extra = "allow"  

    @property
    def database_url(self) -> str:
        return f"mysql+pymysql://{self.mysql_user}:{self.mysql_password}@{self.mysql_host}:{self.mysql_port}/{self.mysql_database}"

@lru_cache()
def get_settings():
    return Settings()
