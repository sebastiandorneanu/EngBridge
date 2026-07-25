# createDB.py
from config.database import Base, engine
from models.user import User, UserRole

Base.metadata.create_all(bind=engine)
print("Database tables created!")
from config.database import SessionLocal
import bcrypt

db = SessionLocal()

admin = db.query(User).filter(User.username=="admin").first()
if not admin:
    hashed_password = bcrypt.hashpw("admin1234".encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
    admin = User(
        username="admin",
        email="admin@example.com",
        password=hashed_password,
        role=UserRole.ADMIN
    )
    db.add(admin)
    db.commit()

db.close()
print("Admin created or already exists")
