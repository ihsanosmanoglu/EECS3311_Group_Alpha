#!/bin/bash

# Simple script to view NutriSci database
# Usage: ./view_db.sh [command]

DB_PATH="./data/nutrisci_db"
H2_JAR="target/dependency/h2-2.2.224.jar"

# Check if H2 jar exists
if [ ! -f "$H2_JAR" ]; then
    echo "Setting up H2 tools..."
    mvn dependency:copy-dependencies -DincludeArtifactIds=h2 -DoutputDirectory=target/dependency
fi

# Simple database viewer function
run_sql() {
    java -cp "$H2_JAR" org.h2.tools.Shell -url "jdbc:h2:file:$DB_PATH" -user sa -sql "$1"
}

# Show menu if no arguments
if [ $# -eq 0 ]; then
    echo "🗄️  NutriSci Database Viewer"
    echo "Usage: ./view_db.sh [option]"
    echo ""
    echo "Options:"
    echo "  tables     - Show all tables"
    echo "  profiles   - Show all profiles"
    echo "  meals      - Show all meals"
    echo "  count      - Count records"
    echo "  \"SQL\"      - Run custom SQL query"
    echo ""
    echo "Examples:"
    echo "  ./view_db.sh tables"
    echo "  ./view_db.sh profiles"
    echo "  ./view_db.sh \"SELECT * FROM MEALS LIMIT 3;\""
    exit 0
fi

# Handle different commands
case $1 in
    "tables")
        echo "📋 Database Tables:"
        run_sql "SHOW TABLES;"
        ;;
    "profiles")
        echo "👤 User Profiles:"
        run_sql "SELECT * FROM PROFILES;"
        ;;
    "meals")
        echo "🍽️  Meals Data:"
        run_sql "SELECT * FROM MEALS;"
        ;;
    "count")
        echo "📊 Record Counts:"
        run_sql "SELECT 'PROFILES' as TABLE_NAME, COUNT(*) as COUNT FROM PROFILES UNION SELECT 'MEALS', COUNT(*) FROM MEALS;"
        ;;
    *)
        echo "🔍 Running SQL: $1"
        run_sql "$1"
        ;;
esac 