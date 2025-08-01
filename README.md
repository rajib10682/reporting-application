# Reporting Application

A comprehensive reporting application with Angular frontend and Spring Boot microservices backend, featuring dynamic SQL generation and advanced queuing capabilities.

## Architecture

- **Frontend**: Angular 17+ with Material Design
- **Backend**: Spring Boot microservices
- **Database**: PostgreSQL 15
- **Queue**: Redis 7
- **Discovery**: Eureka Server

## Services

1. **Gateway Service** (Port 8080) - API Gateway
2. **User Service** (Port 8081) - User management
3. **Report Service** (Port 8082) - Report generation and metadata
4. **Data Service** (Port 8083) - Data processing

## Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Maven 3.6+
- Angular CLI
- Docker & Docker Compose

### Database Setup
```bash
# Start PostgreSQL and Redis using Docker Compose
docker-compose up -d postgres redis

# Verify services are running
docker-compose ps
```

This will create:
- PostgreSQL database on port 5432 with separate databases: `reportdb`, `userdb`, `datadb`
- Redis cache on port 6379
- Automatic database initialization with business metadata schema

### Backend Setup
```bash
# Start each service
cd backend/gateway-service && mvn spring-boot:run &
cd backend/user-service && mvn spring-boot:run &
cd backend/report-service && mvn spring-boot:run &
cd backend/data-service && mvn spring-boot:run &
```

### Frontend Setup
```bash
cd frontend
npm install
ng serve
```

Access the application at `http://localhost:4200`

## Database Configuration

The application uses PostgreSQL with separate databases for each microservice:

- **reportdb**: Business metadata tables (fxrate_info, scenario_info, account_info, segment_info, geography_info, goc_info, user_info)
- **userdb**: User management data
- **datadb**: Data processing and caching

### Business Metadata Tables

The application supports the following business metadata tables with composite keys and hierarchical relationships:

1. **fxrate_info**: FX rates with composite key (fx_id, fx_name, year, currency)
   - Contains monthly rates (m1_rate through m12_rate)
   
2. **scenario_info**: Scenarios with foreign key to fxrate_info
   - Primary key: scenario_id
   - Unique constraint: scenario_name
   
3. **account_info**: Account hierarchy with composite key (account_id, account_parent_id, period_id)

4. **segment_info**: Segment hierarchy with composite key (segment_id, segment_parent_id, period_id)

5. **geography_info**: Geography hierarchy with composite key (geo_id, geo_parent_id, period_id)

6. **goc_info**: GOC analysis with composite key (goc, segment_id, geo_id, period_id)
   - Foreign keys to segment_info and geography_info

7. **user_info**: User management with primary key (user_id)

## Features

### Dynamic Report Generation
- Dynamically prepares SQL queries based on user selections from the UI
- Retrieves data from large datasets, efficiently joining multiple tables
- Allows users to select and configure columns for the report
- Utilizes metadata tables for joins and to provide descriptions for joining keys

### User Interaction
- Provides UI options for users to select criteria and apply filters
- Enables saving report configurations as templates for future reuse

### System Management & Concurrency
- Configurable system-level limit for the number of concurrent reports
- Queues report requests when the concurrent limit is exceeded
- Manages report statuses: Pending, Executing, Completed
- Implements request reprioritization: prioritizes requests from other users if one user already has a report running, queuing subsequent requests from that same user

## Technology Stack

- **Frontend**: Angular 17+ with Material Design components
- **Backend**: Spring Boot 3.2.0 microservices
- **Database**: PostgreSQL 15 with separate databases per microservice
- **Queue**: Redis 7 for concurrency management
- **Discovery**: Eureka Server for service discovery
- **Build**: Maven 3.6+

## Development

### Database Management
```bash
# Stop databases
docker-compose down

# Reset databases (removes all data)
docker-compose down -v
docker-compose up -d postgres redis

# View logs
docker-compose logs postgres
docker-compose logs redis
```

### Connecting to PostgreSQL
```bash
# Using Docker
docker exec -it reporting-postgres psql -U postgres -d reportdb

# Using local psql client
psql -h localhost -p 5432 -U postgres -d reportdb
```

### Running Tests
```bash
# Backend tests
cd backend/report-service && mvn test
cd backend/user-service && mvn test
cd backend/data-service && mvn test

# Frontend tests
cd frontend && npm test
```

### Building for Production
```bash
# Backend
cd backend && mvn clean package

# Frontend
cd frontend && ng build --prod
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

MIT License
