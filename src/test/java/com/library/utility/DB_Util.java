package com.library.utility;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DB_Util {

    private static Connection con ;
    private static Statement stm ;
    private static ResultSet rs ;
    private static ResultSetMetaData rsmd ;

    public static void createConnection(String url , String username, String password){


        try {
            con = DriverManager.getConnection(url, username, password) ;
            System.out.println("CONNECTION SUCCESSFUL");
        } catch (Exception e) {
            System.out.println("CONNECTION HAS FAILED " + e.getMessage() );
        }

    }
    public static void createConnection(){

        String url      = ConfigurationReader.getProperty("library2.db.url") ;
        String username = ConfigurationReader.getProperty("library2.db.username") ;
        String password = ConfigurationReader.getProperty("library2.db.password") ;

        createConnection(url, username, password);

    }

    public static ResultSet runQuery(String sql){

        try {
            stm = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stm.executeQuery(sql); // setting the value of ResultSet object
            rsmd = rs.getMetaData() ;  // setting the value of ResultSetMetaData for reuse
        }catch(Exception e){
            System.out.println("ERROR OCCURRED WHILE RUNNING QUERY "+ e.getMessage() );
        }

        return rs ;

    }

    public static void destroy(){
        // WE HAVE TO CHECK IF WE HAVE THE VALID OBJECT FIRST BEFORE CLOSING THE RESOURCE
        // BECAUSE WE CAN NOT TAKE ACTION ON AN OBJECT THAT DOES NOT EXIST
        try {
            if( rs!=null)  rs.close();
            if( stm!=null)  stm.close();
            if( con!=null)  con.close();
        } catch (Exception e) {
            System.out.println("ERROR OCCURRED WHILE CLOSING RESOURCES " + e.getMessage() );
        }

    }

    private static void resetCursor(){

        try {
            rs.beforeFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static int getRowCount(){

        int rowCount = 0 ;
        try {
            rs.last() ;
            rowCount = rs.getRow() ;
        } catch (Exception e) {
            System.out.println("ERROR OCCURRED WHILE GETTING ROW COUNT " + e.getMessage() );
        }finally {
            resetCursor();
        }

        return rowCount ;

    }

    public static int getColumnCount(){

        int columnCount = 0 ;

        try {
            columnCount = rsmd.getColumnCount();

        } catch (Exception e) {
            System.out.println("ERROR OCCURRED WHILE GETTING COLUMN COUNT " + e.getMessage() );
        }

        return columnCount ;

    }

    public static List<String> getAllColumnNamesAsList(){

        List<String> columnNameLst = new ArrayList<>();

        try {
            for (int colIndex = 1; colIndex <= getColumnCount() ; colIndex++) {
                String columnName =  rsmd.getColumnName(colIndex) ;
                columnNameLst.add(columnName) ;
            }
        }catch (Exception e){
            System.out.println("ERROR OCCURRED WHILE getAllColumnNamesAsList "+ e.getMessage());
        }

        return columnNameLst ;

    }

    public static List<String> getRowDataAsList( int rowNum ){

        List<String> rowDataAsLst = new ArrayList<>();
        int colCount =  getColumnCount() ;

        try {
            rs.absolute( rowNum );

            for (int colIndex = 1; colIndex <= colCount ; colIndex++) {

                String cellValue =  rs.getString( colIndex ) ;
                rowDataAsLst.add(   cellValue  ) ;

            }


        } catch (Exception e) {
            System.out.println("ERROR OCCURRED WHILE getRowDataAsList " + e.getMessage() );
        }finally {
            resetCursor();
        }


        return rowDataAsLst ;
    }


    public static String getCellValue(int rowNum , int columnIndex) {

        String cellValue = "" ;

        try {
            rs.absolute(rowNum) ;
            cellValue = rs.getString(columnIndex ) ;

        } catch (Exception e) {
            System.out.println("ERROR OCCURRED WHILE getCellValue " + e.getMessage() );
        }finally {
            resetCursor();
        }
        return cellValue ;

    }

    public static String getCellValue(int rowNum ,String columnName){

        String cellValue = "" ;

        try {
            rs.absolute(rowNum) ;
            cellValue = rs.getString( columnName ) ;

        } catch (Exception e) {
            System.out.println("ERROR OCCURRED WHILE getCellValue " + e.getMessage() );
        }finally {
            resetCursor();
        }
        return cellValue ;

    }

    public static String getFirstRowFirstColumn(){

        return getCellValue(1,1) ;

    }

    public static List<String> getColumnDataAsList(int columnNum){

        List<String> columnDataLst = new ArrayList<>();

        try {
            rs.beforeFirst(); // make sure the cursor is at before first location
            while( rs.next() ){

                String cellValue = rs.getString(columnNum) ;
                columnDataLst.add(cellValue) ;
            }

        } catch (Exception e) {
            System.out.println("ERROR OCCURRED WHILE getColumnDataAsList " + e.getMessage() );
        }finally {
            resetCursor();
        }


        return columnDataLst ;

    }

    public static List<String> getColumnDataAsList(String columnName){

        List<String> columnDataLst = new ArrayList<>();

        try {
            rs.beforeFirst(); // make sure the cursor is at before first location
            while( rs.next() ){

                String cellValue = rs.getString(columnName) ;
                columnDataLst.add(cellValue) ;
            }

        } catch (Exception e) {
            System.out.println("ERROR OCCURRED WHILE getColumnDataAsList " + e.getMessage() );
        }finally {
            resetCursor();
        }


        return columnDataLst ;

    }

    public static void  displayAllData(){

        int columnCount = getColumnCount() ;
        resetCursor();
        try{

            while(rs.next()){

                for (int colIndex = 1; colIndex <= columnCount; colIndex++) {

                    //System.out.print( rs.getString(colIndex) + "\t" );
                    System.out.printf("%-25s", rs.getString(colIndex));
                }
                System.out.println();

            }

        }catch(Exception e){
            System.out.println("ERROR OCCURRED WHILE displayAllData " + e.getMessage() );
        }finally {
            resetCursor();
        }

    }

    public static Map<String,String> getRowMap(int rowNum){

        Map<String,String> rowMap = new LinkedHashMap<>();
        int columnCount = getColumnCount() ;

        try{

            rs.absolute(rowNum) ;

            for (int colIndex = 1; colIndex <= columnCount ; colIndex++) {
                String columnName = rsmd.getColumnName(colIndex) ;
                String cellValue  = rs.getString(colIndex) ;
                rowMap.put(columnName, cellValue) ;
            }

        }catch(Exception e){
            System.out.println("ERROR OCCURRED WHILE getRowMap " + e.getMessage() );
        }finally {
            resetCursor();
        }


        return rowMap ;
    }

    public static List<Map<String,String>> getAllRowAsListOfMap(){

        List<Map<String,String>> allRowLstOfMap = new ArrayList<>();
        int rowCount = getRowCount() ;
        // move from first row till last row
        // get each row as map object and add it to the list

        for (int rowIndex = 1; rowIndex <= rowCount ; rowIndex++) {

            Map<String,String> rowMap = getRowMap(rowIndex);
            allRowLstOfMap.add( rowMap ) ;

        }
        resetCursor();

        return allRowLstOfMap ;

    }

}
