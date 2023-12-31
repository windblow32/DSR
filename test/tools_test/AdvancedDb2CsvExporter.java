package tools_test;
import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;

/**
 * An advanced Java program that exports data from any table to CSV file.
 * @author Nam Ha Minh
 * (C) Copyright codejava.net
 */
public class AdvancedDb2CsvExporter {
    private BufferedWriter fileWriter;
    public void export(String table) {
        String jdbcURL = "jdbc:mysql://127.0.0.1:3306/sys";
        String username = "tyf";
        String password = "if2366804227";
        String csvFileName = getFileName(table.concat("_Export"));
        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password)) {
            String sql = "SELECT * FROM ".concat(table);
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql);
            fileWriter = new BufferedWriter(new FileWriter(csvFileName));
            int columnCount = writeHeaderLine(result);
            while (result.next()) {
                String line = "";
                for (int i = 2; i <= columnCount; i++) {
                    Object valueObject = result.getObject(i);
                    String valueString = "";
                    if (valueObject != null) valueString = valueObject.toString();
                    if (valueObject instanceof String) {
                        valueString = "\"" + escapeDoubleQuotes(valueString) + "\"";
                    }
                    line = line.concat(valueString);
                    if (i != columnCount) {
                        line = line.concat(",");
                    }
                }
                fileWriter.newLine();
                fileWriter.write(line);
            }
            statement.close();
            fileWriter.close();
        } catch (SQLException e) {
            System.out.println("Datababse error:");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("File IO error:");
            e.printStackTrace();
        }
    }
    private String getFileName(String baseName) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String dateTimeInfo = dateFormat.format(new Date());
        return baseName.concat(String.format("_%s.csv", dateTimeInfo));
    }
    private int writeHeaderLine(ResultSet result) throws SQLException, IOException {
        // write header line containing column names
        ResultSetMetaData metaData = result.getMetaData();
        int numberOfColumns = metaData.getColumnCount();
        String headerLine = "";
        // exclude the first column which is the ID field
        for (int i = 2; i <= numberOfColumns; i++) {
            String columnName = metaData.getColumnName(i);
            headerLine = headerLine.concat(columnName).concat(",");
        }
        fileWriter.write(headerLine.substring(0, headerLine.length() - 1));
        return numberOfColumns;
    }
    private String escapeDoubleQuotes(String value) {
        return value.replaceAll("\"", "\"\"");
    }
    public static void main(String[] args) {
        AdvancedDb2CsvExporter exporter = new AdvancedDb2CsvExporter();
        exporter.export("monkeypox");
    }
}
