package shiftmate.proj;
// Imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Hashtable;
import java.util.LinkedList;
public class DBController {
    // Database connection parameters
    static Connection connection;
    static String url = "jdbc:mysql://dcm.uhcl.edu/sens24g2";
    static String username = "sens24g2";
    static String password = "Sce9902292!!";
    //takes a string query and returns a linked list of hastables where each row is a table of key value pairs -- Written By: Elizabeth
    private static LinkedList<Hashtable<String,String>> getParameterizedQuery(String query, int numParams, String [] param){
        LinkedList<Hashtable<String,String>> list = new LinkedList<>();     
        try {
            // Establishing a connection to the database
            Connection connection = DriverManager.getConnection(url, username, password);
            // If the connection is successful
            System.out.println("Connected to the database.\n\n");
            try {
                PreparedStatement prepstmt = connection.prepareStatement(query);
                try {
                    //fill in each parameter
                    for(int i = 1; i<= numParams; i++){
                        prepstmt.setString(numParams, param[i-1]);
                    }                    
                    ResultSet rs = prepstmt.executeQuery() ;
                    try {
                        while(rs.next()){
                            Hashtable<String, String> currentRowMap = new Hashtable<>();
                            ResultSetMetaData rsmd = rs.getMetaData(); //gets column name
                            int columnCount = rsmd.getColumnCount();
                            for (int i = 1; i <= columnCount; i++) {
                                // retrieves column name and value.
                                String key = rsmd.getColumnLabel(i); //key is column name
                                String value = rs.getString(rsmd.getColumnName(i)); //value is column value
                                if (value == null) {
                                    value = "null";
                                }
                                // builds map.
                                currentRowMap.put(key, value);
                            }
                            list.add(currentRowMap);
                        }
                    } finally {
                        try { rs.close(); } 
                        catch (Throwable ignore) { 
                        // Propagate the original exception instead of this one that you may want just logged  
                        }
                    }
                } finally {
                    try { prepstmt.close(); } 
                    catch (Throwable ignore) { 
                        // Propagate the original exception instead of this one that you may want just logged  
                    }
                }
            } finally {
                //It's important to close the connection when you are done with it
                try { connection.close(); } 
                catch (Throwable ignore) { 
                    // Propagate the original exception instead of this one that you may want just logged  
                }
            }
        } catch (SQLException e) {   
            // Handle any SQL exceptions
            e.printStackTrace();
        }
        return list;
    } //end of class
    // Gets the employee information, and adds to database -- Written By: Elizabeth
    public static boolean addEmployee(String fname, String lname, String phone, String email, String startDate, int depID, String contact, String contactPhone,int employeeID) {
        String query = "INSERT INTO employeeinfo (fname, lname, phone, email, startDate, depID, contact, contactPhone,employeeID) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?)";
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            try (PreparedStatement prepstmt = connection.prepareStatement(query)) {
                connection.setAutoCommit(false);
                prepstmt.setString(1, fname);
                prepstmt.setString(2, lname);
                prepstmt.setString(3, phone);
                prepstmt.setString(4, email);
                prepstmt.setString(5, startDate);
                prepstmt.setInt(6, depID);
                prepstmt.setString(7, contact);
                prepstmt.setString(8, contactPhone);
                prepstmt.setInt(9, employeeID);
    
                int rowsAffected = prepstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    System.out.println("Employee added successfully.");
                    connection.commit();
                    return true;
                } else {
                    System.out.println("Failed to add employee.");
                    return false;
                }
            }
        } catch (SQLException e) {
            try{
                
                System.err.println("Transaction is being rolled back");
                connection.rollback();
             }catch (SQLException excep) {}
            e.printStackTrace();
            return false;
        }
    }
    // Gets the employeeID and returns the employee information -- Written By: Elizabeth
    public static LinkedList<Hashtable<String,String>> getEmployeeInformation(int employeeID){
        String params[] = {Integer.toString(employeeID)};
        return getParameterizedQuery("SELECT e.* FROM employeeinfo as e WHERE employeeid = ?;", 1, params);
    }
    // Gets the employee information, and updates the database -- Written By: Elizabeth
    public static boolean editEmployeeInformation(int employeeID, String fname, String lname, int depID, String phone, String email, String contact, String contactPhone,String startDate) {
        String query = "UPDATE employeeinfo SET fname = ?, lname = ?, depID = ?, phone = ?, email = ?, contact = ?, contactPhone = ?, startDate = ? WHERE employeeid = ?";
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            try (PreparedStatement prepstmt = connection.prepareStatement(query)) {
                connection.setAutoCommit(false);
                prepstmt.setString(1, fname);
                prepstmt.setString(2, lname);
                prepstmt.setInt(3, depID);
                prepstmt.setString(4, phone);
                prepstmt.setString(5, email);
                prepstmt.setString(6, contact);
                prepstmt.setString(7, contactPhone);
                prepstmt.setString(8, startDate);
                prepstmt.setInt(9, employeeID);
    
                int rowsAffected = prepstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    System.out.println("Employee information updated successfully.");
                    connection.commit();
                    return true;
                } else {
                    System.out.println("Employee with ID " + employeeID + " not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            try{
                
                System.err.println("Transaction is being rolled back");
                connection.rollback();
            } catch (SQLException excep) {}
            e.printStackTrace();
            return false;
        }
    }    
    // Gets the name of the employee, and returns the employeeID -- Written By: Kellie
    public static int getEmployeeID(String firstName, String lastName) {
        String query = "SELECT employeeID FROM EmployeeInfo WHERE fname = ? AND lname = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("employeeID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if employee ID is not found or an error occurs
    }
    // Returns the employee information and their deparment name -- Written By: Elizabeth
    public static LinkedList<Hashtable<String,String>> getEmployees(){
        String params[] = {};
        return getParameterizedQuery("SELECT e.*, d.depName FROM employeeinfo e INNER JOIN departments d ON e.depid = d.depid", 0, params);
    }
    // Gets the employeeID and deletes employee -- Written By: Elizabeth
    public static boolean deleteEmployee(int employeeID) {
        String query = "DELETE FROM employeeinfo WHERE employeeID = ?";
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            try (PreparedStatement prepstmt = connection.prepareStatement(query)) {
                connection.setAutoCommit(false);
                prepstmt.setInt(1, employeeID);
                int rowsAffected = prepstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Employee deleted successfully.");
                    connection.commit();
                    return true;
                } else {
                    System.out.println("Employee with ID " + employeeID + " not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            try{
                System.err.println("Transaction is being rolled back");
                connection.rollback();
            } catch (SQLException excep) {}
            e.printStackTrace();
            return false;
        }
    }
    // Gets the department name and the department manager and adds to the database -- Written By: Elizabeth
    public static boolean addDepartment(String depName, String depManager) {
        String query = "INSERT INTO departments (depName, depManager) VALUES (?, ?)";
        Connection connection = null; // Declare connection outside try block
        try {
            connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(false);
            try (PreparedStatement prepstmt = connection.prepareStatement(query)) {
                prepstmt.setString(1, depName);
                prepstmt.setString(2, depManager);
                int rowsAffected = prepstmt.executeUpdate();
                connection.commit();
                if (rowsAffected > 0) {
                    boolean accept = createDefaultDepartmentTable(depName);
                    if (accept) {
                        System.out.println("Department added successfully");
                        return true;
                    } else {
                        System.out.println("Failed to add department.");
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
            try {
                if (connection != null) { // Check if connection is not null before rollback
                    System.err.println("Transaction is being rolled back");
                    connection.rollback();
                }
            } catch (SQLException excep) {
                excep.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.close(); // Close connection in finally block
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false; // Return false in case of any unexpected error
    }    
    // Gets the old department name and the new department name and changes the name of the table -- Written By: Kellie
    public static boolean renameDefaultShiftTable(String oldDepName, String newDepName) {
        String oldTableName = oldDepName + "DefaultSchedule";
        String newTableName = newDepName + "DefaultSchedule";
        String renameQuery = "RENAME TABLE " + oldTableName + " TO " + newTableName;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(renameQuery);
            System.out.println("Default shift table renamed successfully.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to rename default shift table.");
            return false;
        }
    }      
    // Gets the department name and creates the default schedule table -- Written By: Kellie
    private static boolean createDefaultDepartmentTable(String depName) {
        String tableName = depName.concat("DefaultSchedule");
        String shiftIDName = depName + "shiftID";
        String query = "CREATE TABLE " + tableName + " ( " + shiftIDName +
                        " INT AUTO_INCREMENT PRIMARY KEY, " +
                        "DepID INT, " +
                        "DayOfWeek VARCHAR(255), " +
                        "StartTime TIME, " +
                        "EndTime TIME, " +
                        "FOREIGN KEY (DepID) REFERENCES Departments(depID) " +
                        ")";
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(query);
                System.out.println("Table " + tableName + " created successfully.");
                return true;
            }
        } catch (SQLException e) {
            try{
                System.err.println("Transaction is being rolled back");
                connection.rollback();
            } catch (SQLException excep) {}
            e.printStackTrace();
            return false;
        }
    }
    // Gets the department id and returns the department information -- Written By: Elizabeth 
    public static LinkedList<Hashtable<String,String>> getDepartmentInformation(int depID){
        String params[] = {Integer.toString(depID)};
        return getParameterizedQuery("SELECT d.* FROM departments as d WHERE depid = ?;", 1, params);
    }
    // Get the department id, department name, and department manager and updates database -- Written By: Kellie
    public static boolean editDepartmentInformation(int depID, String depName, String depManager) {
        String query = "UPDATE departments SET depName = ?, depManager = ? WHERE depid = ?";
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            try (PreparedStatement prepstmt = connection.prepareStatement(query)) {
                connection.setAutoCommit(false);
                prepstmt.setString(1, depName);
                prepstmt.setString(2, depManager);
                prepstmt.setInt(3, depID);
                int rowsAffected = prepstmt.executeUpdate();
                connection.commit();
                if (rowsAffected > 0) {
                    System.out.println("Department information updated successfully.");
                    return true;
                } else {
                    System.out.println("Department with ID " + depID + " not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            try{
                System.err.println("Transaction is being rolled back");
                connection.rollback();
            } catch (SQLException excep) {}
            e.printStackTrace();
            return false;
        }
    }  
    // Get the departmentID and return the department's employees -- Written By: Elizabeth
    public static LinkedList<Hashtable<String,String>> getDepartmentEmployees(int depID){
        String params[] = {Integer.toString(depID)};
        return getParameterizedQuery("SELECT CONCAT(fname, ' ', lname) AS eName, employeeID FROM employeeinfo WHERE depID = ?", 1, params);
    }
    // Get the departmentID and return the department's employees + emails -- Written By: Elizabeth
    public static LinkedList<Hashtable<String,String>> getDepartmentEmployeesWithEmail(int depID){
        String params[] = {Integer.toString(depID)};
        return getParameterizedQuery("SELECT CONCAT(fname, ' ', lname) AS eName, employeeID, email FROM employeeinfo WHERE depID = ?", 1, params);
    }
    // Return the departments -- Written By: Elizabeth
    public static LinkedList<Hashtable<String,String>> getDepartments(){
        String params[] = {};
        return getParameterizedQuery("SELECT d.* FROM departments d;", 0, params);
    }
    // Get the department id and delete from the database -- Written By: Elizabeth
    public static boolean deleteDepartment(int depID, String depName) {
        String queryDeleteDepartment = "DELETE FROM departments WHERE depID = ?";
        String table = depName.concat("defaultschedule");
        String queryDeleteShifts = "DELETE FROM " + table + " WHERE depID = ?";
        String queryDropTable = "DROP TABLE IF EXISTS " + table; // Ensure the table exists before dropping
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(false);
            // Delete associated records from table
            try (PreparedStatement deleteHelpShiftsStmt = connection.prepareStatement(queryDeleteShifts)) {
                deleteHelpShiftsStmt.setInt(1, depID);
                deleteHelpShiftsStmt.executeUpdate();
            }
            // Delete the department
            try (PreparedStatement deleteDeptStmt = connection.prepareStatement(queryDeleteDepartment)) {
                deleteDeptStmt.setInt(1, depID);
                int rowsAffected = deleteDeptStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Department '" + depName + "' deleted successfully.");
                    // Drop the default schedule table
                    try (PreparedStatement dropTableStmt = connection.prepareStatement(queryDropTable)) {
                        dropTableStmt.executeUpdate();
                    }
                    connection.commit();
                    return true;
                } else {
                    System.out.println("Department with ID " + depID + " not found.");
                }
            }
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    System.err.println("Transaction is being rolled back");
                    connection.rollback();
                }
            } catch (SQLException excep) {
                excep.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false; // Return false outside the try-catch-finally block
    }
    // Get the department name and return the departments default schedule -- Written By: Kellie
    public static LinkedList<Hashtable<String,String>> getDefaultSchedule(String depName){
        System.out.println(depName);
        String table = depName.concat("defaultschedule");
        String query = "SELECT * FROM " + table;
        return getParameterizedQuery(query, 0, null);
    }
    // Get the shift details and returns the scheduleid -- Written By: Kellie
    public static int addShiftDefaultSchedule(String depName, String dayOfWeek, String startTime, String endTime) {
        String table = depName.concat("defaultschedule");
        String addShiftQuery = "INSERT INTO " + table + " (depID, dayOfWeek, startTime, endTime) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            // Get department ID
            int depID = getDepartmentID(depName);
            if (depID != -1) {
                // Department exists, proceed to add shift
                try (PreparedStatement preparedStatement = connection.prepareStatement(addShiftQuery, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setInt(1, depID);
                    preparedStatement.setString(2, dayOfWeek);
                    preparedStatement.setString(3, startTime);
                    preparedStatement.setString(4, endTime);
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int scheduleID = generatedKeys.getInt(1);
                            System.out.println("Shift added successfully with scheduleID: " + scheduleID);
                            return scheduleID;
                        } else {
                            System.out.println("Failed to retrieve scheduleID.");
                            return -1;
                        }
                    } else {
                        System.out.println("Failed to add shift.");
                        return -1;
                    }
                }
            } else {
                System.out.println("Department does not exist.");
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }    
    // Get the department name and return the department id -- Written By: Kellie
    public static int getDepartmentID(String depName) {
        String params[] = {depName};
        LinkedList<Hashtable<String, String>> result = getParameterizedQuery("SELECT depID FROM departments WHERE depName = ?;", 1, params);
        // Check if the result is not empty and contains the "depID" key
        if (!result.isEmpty() && result.getFirst().containsKey("depID")) {
            String depIDString = result.getFirst().get("depID");
            try {
                return Integer.parseInt(depIDString);
            } catch (NumberFormatException e) {
                // Handle the case where the depID cannot be parsed to an integer
                e.printStackTrace();
                return -1; // Return -1 to indicate failure
            }
        } else {
            // Handle the case where the result is empty or depID is not found
            System.err.println("Department ID not found for department: " + depName);
            return -1; // Return -1 to indicate failure
        }
    }    
    // Get the shift details and update the database -- Written By: Kellie
    public static boolean editShiftDefaultSchedule(String depName, String oldStartTime, String oldEndTime, String dayOfWeek, String newStartTime, String newEndTime){
        String table = depName.concat("defaultschedule");
        String updateShiftQuery = "UPDATE " + table + " SET startTime = ?, endTime = ? WHERE dayOfWeek = ? AND startTime = ? AND endTime = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateShiftQuery)) {
                preparedStatement.setString(1, newStartTime);
                preparedStatement.setString(2, newEndTime);
                preparedStatement.setString(3, dayOfWeek);
                preparedStatement.setString(4, oldStartTime);
                preparedStatement.setString(5, oldEndTime);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Shift updated successfully.");
                    return true;
                } else {
                    System.out.println("Failed to update shift.");
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Delete shift from default department schedule -- Written By: Kellie
    public static boolean deleteShiftDefaultSchedule(String depName, String dayOfWeek, String startTime, String endTime){
        String table = depName.concat("defaultschedule");
        String deleteShiftQuery = "DELETE FROM " + table + " WHERE dayOfWeek = ? AND startTime = ? AND endTime = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteShiftQuery)) {
                preparedStatement.setString(1, dayOfWeek);
                preparedStatement.setString(2, startTime);
                preparedStatement.setString(3, endTime);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Shift deleted successfully.");
                    return true;
                } else {
                    System.out.println("Failed to delete shift.");
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Create the weekly schedule table -- Written By: Kellie
    public static boolean createWeeklyScheduleTable(String depName) {
        String tableName = depName.concat("WeeklySchedule");
        String shiftIDName = depName + "ShiftID";
        // Query to drop the table if it exists
        String dropQuery = "DROP TABLE IF EXISTS " + tableName;
        // Query to create the new table
        String query = "CREATE TABLE " + tableName + " ( " + shiftIDName +
                        " INT AUTO_INCREMENT PRIMARY KEY, " +
                        "DepID INT, " + 
                        "WeekStartDate DATE, " +  // Add column for week start date
                        "EmployeeID INT, " +
                        "DayOfWeek VARCHAR(255), " +
                        "StartTime TIME, " +
                        "EndTime TIME, " +
                        "FOREIGN KEY (DepID) REFERENCES Departments(depID), " +
                        "FOREIGN KEY (EmployeeID) REFERENCES EmployeeInfo(employeeID)" +
                        ")";
        
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement stmt = connection.createStatement()) {
            // Drop the table if it exists
            stmt.executeUpdate(dropQuery);
            // Create the new table
            stmt.executeUpdate(query);
            System.out.println("Table " + tableName + " created successfully.");
            createWeeklySchedule(depName);
            // CHECK IF IT WORKED
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Get the department name and return the employee availabilities -- Written By: Kellie
    public static LinkedList<Hashtable<String,String>> getAvailabilities(String depName){
        String query = "SELECT a.*, e.fname, e.lname, e.maxWeeklyHours FROM availability AS a " +
                    "INNER JOIN employeeinfo AS e ON a.employeeID = e.employeeID " +
                    "INNER JOIN departments AS d ON e.depID = d.depID " +
                    "WHERE d.depName = ?";
        String params[] = {depName};
        return getParameterizedQuery(query, 1, params);
    }
    // Get the department name and build the weekly schedule-- Written By: Kellie
    private static Boolean createWeeklySchedule(String depName) {
        LinkedList<Hashtable<String, String>> weeklySchedule = CreateWeeklyScheduleUtils.buildWeeklySchedule(depName);
        boolean allShiftsAdded = true; // Flag to track if all shifts were successfully added
        // NEED TO FIX FOR IF ANY DATA IS EMPTY
        for (Hashtable<String, String> shift : weeklySchedule) {
            int employeeID = Integer.parseInt(shift.get("EmployeeID"));
            String dayOfWeek = shift.get("DayOfWeek");
            String startTime = shift.get("StartTime");
            String endTime = shift.get("EndTime");
            int scheduleID = addShiftWeeklySchedule(depName, employeeID, dayOfWeek, startTime, endTime);
            if (scheduleID == -1) {
                allShiftsAdded = false;
                break; // Exit the loop if any shift fails to be added
            }
        }
        return allShiftsAdded;
    }
    // Get the department name and return the weekly schedule -- Written By: Kellie
    public static LinkedList<Hashtable<String,String>> getWeeklySchedule(String depName){
        System.out.println(depName);
        String table = depName.concat("weeklyschedule");
        String query = "SELECT s.*, e.fname, e.lname FROM " + table +" AS s INNER JOIN employeeinfo as e on (s.employeeid = e.employeeid)";
        return getParameterizedQuery(query, 0, null);
    }
    // Get the department name and the employee id and return the employees schedule --Written By: Elizabeth
    public static LinkedList<Hashtable<String,String>> getEmployeeWeeklySchedule(String depName, int employeeID){
        String params[] = {Integer.toString(employeeID)};
        String table = depName.concat("weeklyschedule");
        String query = "SELECT s.*, e.fname, e.lname FROM " + table +" AS s INNER JOIN employeeinfo as e on (s.employeeid = e.employeeid) WHERE e.employeeid = ?";
        return getParameterizedQuery(query, 1, params);
    }
    // Get the department name, employee id, and week start date and return the employees scheudle --Written By: Elizabeth
    public static LinkedList<Hashtable<String,String>> getEmployeeScheduleWeekOf(String depName, int employeeID){
        String params[] = {Integer.toString(employeeID)};
        String table = depName.concat("weeklyschedule");
        String query = "SELECT s.*, e.fname, e.lname FROM " + table +" AS s INNER JOIN employeeinfo as e on (s.employeeid = e.employeeid) WHERE e.employeeid = ?";
        return getParameterizedQuery(query, 1, params);
    }
    // Get the shift details and add shif to the weekly schedule -- Written By: Kellie
    public static int addShiftWeeklySchedule(String depName, int employeeID, String dayOfWeek, String startTime, String endTime) {
        String table = depName.concat("weeklyschedule");
        String addShiftQuery = "INSERT INTO " + table + " (depID, WeekStartDate, employeeID, dayOfWeek, startTime, endTime) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            // Get department ID
            int depID = getDepartmentID(depName);
            if (depID != -1) {
                // Department exists, proceed to add shift
                try (PreparedStatement preparedStatement = connection.prepareStatement(addShiftQuery, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setInt(1, depID);
                    preparedStatement.setInt(3, employeeID);
                    preparedStatement.setString(4, dayOfWeek);
                    preparedStatement.setString(5, startTime);
                    preparedStatement.setString(6, endTime);
                    LocalDate today = LocalDate.now();
                    LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String nextMondaySQL = nextMonday.format(formatter);
                    preparedStatement.setString(2, nextMondaySQL);
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int scheduleID = generatedKeys.getInt(1);
                            System.out.println("Shift added successfully with scheduleID: " + scheduleID);
                            return scheduleID;
                        } else {
                            System.out.println("Failed to retrieve scheduleID.");
                            return -1;
                        }
                    } else {
                        System.out.println("Failed to add shift.");
                        return -1;
                    }
                }
            } else {
                System.out.println("Department does not exist.");
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    // Get the shift details and update shift in weekly schedule -- Written By: Kellie
    public static boolean editShiftWeeklySchedule(String depName, int oldEmployeeID, String oldStartTime, String oldEndTime, String dayOfWeek, int newEmployeeID, String newStartTime, String newEndTime) {
        String table = depName + "weeklyschedule";
        String updateShiftQuery = "UPDATE " + table + " SET startTime = ?, endTime = ?, employeeID = ? WHERE dayOfWeek = ? AND startTime = ? AND endTime = ? AND employeeID = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateShiftQuery)) {
                preparedStatement.setString(1, newStartTime);
                preparedStatement.setString(2, newEndTime);
                preparedStatement.setInt(3, newEmployeeID); // Set the new employee ID
                preparedStatement.setString(4, dayOfWeek);
                preparedStatement.setString(5, oldStartTime);
                preparedStatement.setString(6, oldEndTime);
                preparedStatement.setInt(7, oldEmployeeID); // Use the old employee ID in the WHERE clause
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Shift updated successfully.");
                    return true;
                } else {
                    System.out.println("Failed to update shift.");
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 }    