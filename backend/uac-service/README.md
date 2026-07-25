Iată un README concis, focusat pe cum să pornească serviciul UAC:

UAC Service - Setup și Pornire
Cerințe
Python 3.13+
MySQL (oprit pentru development local)
Git
Quick Start
1. Setup inițial

# Clonează repo-ul (dacă nu l-ai făcut deja)
git clone <repo-url>
cd backend/uac-service

# Creează mediul virtual
python -m venv venv

# Activează mediul virtual
venv\Scripts\activate
2. Instalează dependențele

pip install -r requirements.txt
3. Generează fișierele Protocol Buffers

python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. protos/auth.proto
4. Configurare (opțional)
Creează fișierul config/.env dacă ai nevoie de configurații custom:


DATABASE_URL=mysql+pymysql://user:password@localhost:3306/database_name
JWT_SECRET_KEY=your-secret-key
GRPC_PORT=50051
HTTP_PORT=8000
5. Pornește serverul

python server.py
Serverul va rula pe:

gRPC: localhost:50051
REST API: http://localhost:8000
Troubleshooting
Eroare: ModuleNotFoundError: No module named 'google'


python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. protos/auth.proto
Eroare: Pachete lipsă


pip install -r requirements.txt
Eroare: Database connection

Verifică că MySQL rulează
Verifică credențialele în config/.env
Notițe Importante
⚠️ NU edita manual fișierele auth_pb2.py și auth_pb2_grpc.py - sunt generate automat din auth.proto

⚠️ Dacă modifici protos/auth.proto, trebuie să regenerezi fișierele cu comanda din pasul 3

⚠️ Asigură-te că folosești mediul virtual activat (venv\Scripts\activate)

