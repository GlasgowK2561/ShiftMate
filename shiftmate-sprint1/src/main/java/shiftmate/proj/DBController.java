package shiftmate.proj;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.LinkedList;
/* PUBLIC CLASSES WITHIN THE DATABASE CONTROLLER
addEmployee
    Function:
    Parameters:
    Returns:
getEmployeeInformation
    Function:
    Parameters:
    Returns:
editEmployeeInformation
    Function:
    Parameters:
    Returns:
deleteEmployee
    Function:
    Parameters:
    Returns:
addDepartment
    Function:
    Parameters:
    Returns:
renameDefaultShiftTable
    Function:
    Parameters:
    Returns:
getDepartmentInformation
    Function:
    Parameters:
    Returns:
editDepartmentInformation
    Function:
    Parameters:
    Returns:
getDepartments
    Function:
    Parameters:
    Returns:
deleteDepartment
    Function:
    Parameters:
    Returns:
getDefaultSchedule
    Function:
    Parameters:
    Returns:
getDepartmentEmployees
    Function:
    Parameters:
    Returns:
 */
 public class DBController {
    // Database connection parameters
         
    static Connection connection;
    static String url = "jdbc:mysql://dcm.uhcl.edu/sens24g2";
    static String username = "sens24g2";
    static String password = "Sce9902292!!";
 
    //takes a string query and returns a linked list of hastables where each row is a table of key value pairs 
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
    
    public static LinkedList<Hashtable<String,String>> getEmployeeInformation(int employeeID){
        String params[] = {Integer.toString(employeeID)};
        return getParameterizedQuery("SELECT e.* FROM employeeinfo as e WHERE employeeid = ?;", 1, params);
    }
   
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

    public static LinkedList<Hashtable<String,String>> getEmployees(){
        String params[] = {};
        return getParameterizedQuery("SELECT e.*, d.depName FROM employeeinfo e INNER JOIN departments d ON e.depid = d.depid", 0, params);
    }

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
    
    private static boolean createDefaultDepartmentTable(String depName) {
        String tableName = depName.concat("DefaultSchedule");
        String query = "CREATE TABLE " + tableName + " ( " +
                        "ShiftID INT PRIMARY KEY, " +
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
        
    public static LinkedList<Hashtable<String,String>> getDepartmentInformation(int depID){
        String params[] = {Integer.toString(depID)};
        return getParameterizedQuery("SELECT d.* FROM departments as d WHERE depid = ?;", 1, params);
    }

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

    public static LinkedList<Hashtable<String,String>> getDepartments(){
        String params[] = {};
        return getParameterizedQuery("SELECT d.* FROM departments d;", 0, params);
    }

    public static boolean deleteDepartment(int depID) {
        String querySelect = "SELECT depName FROM departments WHERE depID = ?";
        String queryDelete = "DELETE FROM departments WHERE depID = ?";
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(false);
    
            // Retrieve department name associated with the department ID
            String depName = null;
            try (PreparedStatement selectStmt = connection.prepareStatement(querySelect)) {
                selectStmt.setInt(1, depID);
                ResultSet resultSet = selectStmt.executeQuery();
                if (resultSet.next()) {
                    depName = resultSet.getString("depName");
                }
            }
    
            // If department name is retrieved, proceed with deletion
            if (depName != null) {
                try (PreparedStatement deleteStmt = connection.prepareStatement(queryDelete)) {
                    deleteStmt.setInt(1, depID);
                    int rowsAffected = deleteStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Department '" + depName + "' deleted successfully.");
                        // Call deleteDefaultScheduleTable function using department name
                        boolean affirm = deleteDefaultScheduleTable(connection, depName);
                        connection.commit();
                        return true;
                    } else {
                        System.out.println("Department with ID " + depID + " not found.");
                        return false;
                    }
                }
            } else {
                System.out.println("Department with ID " + depID + " not found.");
                return false;
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
            return false;
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
    }
    
    private static boolean deleteDefaultScheduleTable(Connection connection, String depName) {
        String table = "defaultschedule";
        String defaultScheduleTableName = depName + table;
        String queryDelete = "DROP TABLE " + defaultScheduleTableName;
        
        try (Statement statement = connection.createStatement()) {
            int rowsAffected = statement.executeUpdate(queryDelete);
            if (rowsAffected > 0) {
                System.out.println("Department schedule table '" + defaultScheduleTableName + "' deleted successfully.");
                return true;
            } else {
                System.out.println("Department schedule table with name '" + defaultScheduleTableName + "' not found.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }    

    // Get shifts from default department schedule
    public static LinkedList<Hashtable<String,String>> getDefaultSchedule(String depName){
        System.out.println("GETTING DEFAULT SCHEDULE");
        System.out.println("Value in DBController.java");
        System.out.println(depName);
        String table = "defaultschedule";
        String params[] = {depName.concat(table)};
        return getParameterizedQuery("SELECT d.* FROM ? as d;", 1, params);       
    }
    // Add shift to default department schedule
    // Edit shift in default department schedule
    // Delete shift from default department schedule
    // Create weekly schedule
    // Get weekly schedule
    // Edit weekly schedule
    // Delete weekly schedule
    public static LinkedList<Hashtable<String,String>> getDepartmentEmployees(int depID){
        String params[] = {Integer.toString(depID)};
        return getParameterizedQuery("SELECT CONCAT(fname, ' ', lname) AS eName, employeeID FROM employeeinfo WHERE depID = ?", 1, params);
    } 
 }