# FurEver - Pet Adoption System

## Project Description
FurEver is a desktop application for managing pet adoption. The system uses a web API to communicate with a server for data management.
Users can view available pets, publish pets for adoption, and send adoption requests to pet owners. The application includes both a user interface for regular users and an admin interface for system management.

## Technologies
- **Client**: JavaFX with Java 17
- **Server**: Java's built-in HTTP server with REST API
- **Database**: MySQL 8.0+
- **Build**: Maven (using local mvnd - Maven Daemon for faster builds)
- **Architecture**: Client-Server with HTTP communication
- **Authentication**: JWT (JSON Web Tokens) for secure API access
- **Security**: BCrypt for password hashing

## Development Environment
- **Operating System**: Windows 10/11
- **Architecture**: 64-bit (x64)
- **Java Version**: 17 or higher
- **Database**: MySQL 8.0+

## Development Tools
- **IDEs**: VSCode, IntelliJ IDEA, Eclipse
- **Linting**: Maven compiler plugin (Java)
- **Version Control**: Git

## Project Structure
```
src/main/java/com/furever/
├── client/                    # Client application
│   ├── FurEverApp.java       # Main entry point
│   ├── communication/        # HTTP client for API communication
│   ├── logic/                # Client business logic
│   └── ui/                   # JavaFX controllers and FXML
├── server/                    # Server application
│   ├── api/                  # HTTP server and REST API endpoints
│   ├── logic/                # Server business logic
│   ├── util/                 # Server utilities (password hashing)
│   └── data/                 # Database access layer (DAO)
└── common/                    # Shared components
    └── models/               # Data models
```

## Database Setup

1. Install MySQL Server if not already installed
2. During installation, set a root password (you'll need this later). Remember it, and if you choose a different password, update it in both the batch files and `DatabaseConnection.java`
3. Create the database and tables using the provided schema:

```cmd
mysql -u root -p < src\main\resources\schema.sql
```

Or manually execute the SQL commands in `src\main\resources\schema.sql` using MySQL Workbench

4. Update database connection details in `src\main\java\com\furever\server\data\DatabaseConnection.java` if needed:
   - URL: `jdbc:mysql://localhost:3306/furever`
   - Username: `root` (update if using different MySQL username)
   - Password: (update with the password you set during installation)

**Note**: Connection details are in `DatabaseConnection.java` - update the `USERNAME` and `PASSWORD` fields with your MySQL credentials

## Dependencies

The project uses Maven for dependency management. Key dependencies include:

### Core Dependencies
- **JavaFX 17.0.2** - UI framework for the desktop application
- **MySQL Connector 8.0.33** - Database connectivity
- **Gson 2.10.1** - JSON serialization/deserialization
- **JWT (JJWT) 0.11.5** - JWT token generation and validation for authentication
- **BCrypt 0.10.2** - Secure password hashing with salt

### Build Plugins
- **Maven Compiler Plugin 3.11.0** - Java compilation
- **JavaFX Maven Plugin 0.0.8** - JavaFX application execution
- **Maven Shade Plugin 3.5.0** - Creates executable JAR with dependencies

## Building the Project

### Quick Start with Batch Files
```cmd
setup.bat           # Build the project
run-server.bat      # Start the server
run-client.bat      # Start the client application
```

### Manual Build with Maven
```cmd
installations\maven-mvnd-1.0.6-windows-amd64\bin\mvnd.cmd clean install
```

### Running the Server
```cmd
java -jar target\furever-server.jar
```

### Running the Client
```cmd
installations\maven-mvnd-1.0.6-windows-amd64\bin\mvnd.cmd javafx:run
```

## Default Credentials

### Admin User
- **Username**: `admin`
- **Password**: `admin123`

### Test Users
- **test1**: Password `123456`
- **test2**: Password `123456`

There's a sample adoption from test2 attempting to adopt test1's pet (Ginger).

**Note**: Default users are created in the database schema file with BCrypt-hashed passwords for security.

### Admin Registration
- Use the admin code displayed in server logs at startup to register additional admin accounts
- The admin code is randomly generated each time the server starts
- Enter the code in the optional "Admin Code" field during registration
- The code is only available in server logs for security (no API endpoint to retrieve it)
- Regular users can register without entering an admin code

### User Registration
Register through the application interface without an admin code

## Features

### User Interface
- **Login/Register**: User authentication with BCrypt password hashing
- **Pet Browsing**: View all available pets for adoption
- **Search & Filter**: Search pets by name, category, age, and gender
- **Pet Details**: View detailed information about pets (double-click to open details)
- **Adoption Requests**: Send adoption requests to pet owners
- **Add Pet**: Publish pets for adoption
- **Session Management**: Automatic session timeout detection and user-friendly error messages

### Admin Interface
- **User Management**: View and manage registered users
- **Pet Management**: View all pets, update status, delete inappropriate ads
- **Request Management**: View adoption requests, approve/reject requests
- **Double-click Details**: View detailed information by double-clicking rows in tables

### Security Features
- **JWT Authentication**: Bearer token-based API authentication
- **BCrypt Password Hashing**: Secure password storage with salt
- **Authorization Checks**: Role-based access control (admin vs user)
- **Ownership Validation**: Users can only modify their own pets
- **SQL Injection Protection**: Prepared statements for all database queries
- **Input Validation**: Basic validation for required fields
- **Self-Adoption Prevention**: Users cannot adopt their own pets
- **Duplicate Request Prevention**: Users cannot send multiple requests for the same pet

## Database Schema

### Tables
- **Category**: Pet categories (dog, cat, rabbit, parrot, etc.)
- **Pet**: Pet information including owner details
- **AdoptionRequest**: Adoption requests from users
- **User**: User accounts with BCrypt-hashed passwords

## API Endpoints

### Authentication
- `POST /api/auth` - Login (returns JWT token)

### Pets
- `GET /api/pets` - Get all available pets (public)
- `GET /api/pets/{id}` - Get pet by ID (public)
- `GET /api/pets/search` - Search pets with filters (public)
- `POST /api/pets` - Add new pet (requires authentication)
- `PUT /api/pets/{id}` - Update pet (requires authentication + ownership check)
- `PUT /api/pets/{id}/status` - Update pet status (requires authentication + ownership check)
- `DELETE /api/pets/{id}` - Delete pet (requires authentication + ownership check)

### Categories
- `GET /api/categories` - Get all categories (public)

### Adoption Requests
- `GET /api/requests` - Get all requests (admin only, requires authentication)
- `GET /api/requests/{id}` - Get request by ID (requires authentication)
- `GET /api/requests/user/{email}` - Get requests for specific user (requires authentication + email ownership)
- `GET /api/requests/for-user/{email}` - Get user's own requests (requires authentication + email match)
- `POST /api/requests` - Create new request (requires authentication)
- `PUT /api/requests/{id}/approve` - Approve request (requires authentication + pet ownership)
- `PUT /api/requests/{id}/reject` - Reject request (requires authentication + pet ownership)
- `PUT /api/requests/{id}/status` - Set request status (requires authentication + ownership/admin)
- `DELETE /api/requests/{id}` - Delete request (requires authentication + ownership/admin)

### Users
- `GET /api/users` - Get all users (admin only, requires authentication)
- `POST /api/users` - Register new user (public)

## Security Implementation

### Authentication & Session Management
- **JWT Authentication**: Bearer token-based API authentication with 24-hour expiration
- **BCrypt Password Hashing**: Secure password storage with salt
- **Dynamic Key Generation**: Server restarts invalidate all tokens for enhanced security
- **Auto-Timeout Detection**: Client automatically detects session expiration and redirects to login
- **Hebrew Error Messages**: User-friendly error messages in Hebrew for session issues

### Authorization Model
- **Admin users**: Full access to all endpoints including user management
- **Regular users**: Can only modify their own pets and manage adoption requests for their pets
- **Public access**: Read-only access to pets, categories, and public registration/login endpoints

### Data Protection
- **SQL Injection Protection**: Prepared statements for all database queries
- **Input Validation**: Basic validation for required fields on both client and server
- **Self-Adoption Prevention**: Server-side validation prevents users from adopting their own pets
- **Duplicate Request Prevention**: Users cannot send multiple requests for the same pet

## Troubleshooting

### Common Issues

**Database Connection**
- Ensure MySQL server is running
- Verify credentials in `DatabaseConnection.java` (USERNAME and PASSWORD fields)
- Update credentials in batch files if needed
- Run `schema.sql` manually in MySQL Workbench if database doesn't exist

**Port Conflicts**
- Server uses port 8080 by default
- Change port in `FurEverServer.java` if 8080 is in use

**Login Problems**
- Ensure database setup with `schema.sql` file
- Verify database connection credentials
- Default credentials: `admin/admin123`, `test1/123456`, `test2/123456`

**JavaFX Issues**
- Ensure Java 17 or higher is installed
- Try updating Java if experiencing issues on Windows

**MySQL Installation**
- MUST set root password in Accounts and Roles tab during installation
- Next button stays grayed until password field is completed
- No need to add extra users, just set root password

**Session Timeout**
- Expected behavior after server restart (dynamic JWT key generation)
- Message: "ההתחברות פגה - אנא התחבר מחדש"
- Simply log in again after server restart

## Team
- **Group**: 780-60
- **Names**: דניאל מנחם ושלי סייאה
