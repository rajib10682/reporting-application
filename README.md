# Reporting Application

A comprehensive reporting application for data analysis and visualization.

## Features

- Interactive dashboards
- Data visualization charts
- Report generation
- Export functionality
- User management
- Real-time data updates

## Tech Stack

- **Frontend**: Angular with TypeScript, Angular Material
- **Backend**: Spring Boot Microservices (Java)
- **Database**: H2 (in-memory for development)
- **Charts**: Chart.js with ng2-charts
- **UI Components**: Angular Material

## Getting Started

### Prerequisites

- Node.js (v18 or higher)
- Java 17 or higher
- Maven 3.6 or higher

### Installation

1. Clone the repository:
```bash
git clone https://github.com/rajib10682/reporting-application.git
cd reporting-application
```

2. Install frontend dependencies:
```bash
cd frontend
npm install
```

3. Build and run backend services:
```bash
# Start each microservice in separate terminals
cd backend/user-service && mvn spring-boot:run
cd backend/report-service && mvn spring-boot:run
cd backend/data-service && mvn spring-boot:run
cd backend/gateway-service && mvn spring-boot:run
```

4. Run the frontend:
```bash
cd frontend
ng serve
```

## Project Structure

```
reporting-application/
├── frontend/                    # Angular frontend application
├── backend/                     # Spring Boot microservices
│   ├── user-service/           # User management service
│   ├── report-service/         # Report generation service
│   ├── data-service/           # Data processing service
│   └── gateway-service/        # API Gateway service
├── docs/                       # Documentation
├── scripts/                    # Utility scripts
└── README.md
```

## Microservices

- **User Service** (Port 8081): Manages user accounts and authentication
- **Report Service** (Port 8082): Handles report creation and management
- **Data Service** (Port 8083): Processes and analyzes data
- **Gateway Service** (Port 8080): API Gateway for routing requests

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

MIT License
