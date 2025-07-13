# NutriSci Data Storage Guide

## 📂 Where Your Data is Stored

### **Database Location**
Your NutriSci application uses an **H2 embedded database** that stores all data locally on your computer:

- **📍 Location**: `./data/` folder in your project directory
- **🗃️ Files**: 
  - `nutrisci_db.mv.db` - Main database file
  - `nutrisci_db.trace.db` - Database log file (if present)
  - `nutrisci_db.lock.db` - Lock file (temporary, while app is running)

### **What Data is Stored**

1. **👤 User Profiles**
   - Name, age, sex, weight, height
   - Preferred units (metric/imperial)
   - Active profile status
   - Creation timestamps

2. **🍽️ Meal Logs**
   - Meal type (breakfast, lunch, dinner, snack)
   - Date and ingredients
   - Quantities and nutrition data
   - Complete meal history

3. **🔄 User Preferences** (for future features)
   - App settings and preferences

## 🔧 Managing Your Data

### **Backup Your Data**
To backup your nutrition data:
```bash
# Copy the entire data folder
cp -r ./data/ ./data_backup/
```

### **Reset/Clear Data**
To start fresh (⚠️ **Warning: This deletes all your data!**):
```bash
# Stop the application first, then:
rm -rf ./data/
```

### **View Data Location**
```bash
# Check if data exists
ls -la ./data/

# See database file size
du -h ./data/nutrisci_db.mv.db
```

## 🚀 Database Features

### **Advantages of H2 Database**
- ✅ **Fast**: Embedded database with no server setup
- ✅ **Reliable**: ACID transactions ensure data integrity
- ✅ **Portable**: Single file you can backup/share
- ✅ **No Dependencies**: Works offline, no internet required

### **Database Configuration**
- **Type**: H2 Embedded Database
- **Connection**: `jdbc:h2:file:./data/nutrisci_db`
- **Auto-Server**: Enabled (allows multiple connections)
- **Auto-Commit**: Managed per transaction

### **Switching Databases** (Advanced)
You can switch to other databases by editing:
```
src/main/resources/database/database.properties
```

Supported databases:
- **H2** (default) - Embedded, no setup required
- **MySQL** - Requires MySQL server
- **PostgreSQL** - Requires PostgreSQL server  
- **SQLite** - Single file database

## 🔍 Data Security

### **Privacy**
- All data stored locally on your computer
- No data sent to external servers
- No internet connection required for basic functionality

### **Integrity**
- Database transactions ensure data consistency
- Automatic backup of previous state during updates
- Error recovery mechanisms in place

## 🆘 Troubleshooting Data Issues

### **Profile Activation Problems**
✅ **Fixed!** The profile activation issue has been resolved by improving transaction handling.

### **If Database Gets Corrupted**
```bash
# 1. Stop the application
# 2. Backup current data
cp -r ./data/ ./data_backup_$(date +%Y%m%d)/

# 3. Try database repair (if you have H2 tools)
# Or simply delete and restart fresh:
rm -rf ./data/
```

### **Lost Data Recovery**
- Check `./data_backup/` folders
- Look for `.mv.db` files in your system
- Database files are usually 100KB+ in size

## 📊 Data Growth

### **Estimated Storage Usage**
- **Empty database**: ~50KB
- **1 profile + 30 days of meals**: ~100KB
- **Multiple profiles + 1 year of data**: ~500KB-1MB

The database is very efficient and won't take up significant disk space.

## 🔄 Data Migration

### **Moving Data to New Computer**
```bash
# On old computer
cp -r ./data/ ./nutrisci_data_export/

# On new computer (after setting up NutriSci)
cp -r ./nutrisci_data_export/ ./data/
```

### **Sharing Data**
You can share your meal database with others by copying the `data` folder, but remember it contains personal health information.

---
**Note**: Always backup your data before making major changes to the application! 