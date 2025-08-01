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

- **Frontend**: React with TypeScript, Tailwind CSS
- **Backend**: FastAPI (Python)
- **Database**: PostgreSQL
- **Charts**: Recharts
- **UI Components**: shadcn/ui

## Getting Started

### Prerequisites

- Node.js (v18 or higher)
- Python (v3.8 or higher)
- PostgreSQL

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

3. Install backend dependencies:
```bash
cd ../backend
pip install -r requirements.txt
```

4. Set up environment variables:
```bash
cp .env.example .env
# Edit .env with your configuration
```

5. Run the application:
```bash
# Start backend
cd backend
python main.py

# Start frontend (in another terminal)
cd frontend
npm run dev
```

## Project Structure

```
reporting-application/
├── frontend/           # React frontend application
├── backend/           # FastAPI backend application
├── docs/             # Documentation
├── scripts/          # Utility scripts
└── README.md
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

MIT License
