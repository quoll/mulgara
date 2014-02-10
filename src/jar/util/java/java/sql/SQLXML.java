package java.sql;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import javax.xml.transform.Source;
import javax.xml.transform.Result;

public interface SQLXML {
    public void free() throws SQLException;
    public InputStream getBinaryStream() throws SQLException;
    public OutputStream setBinaryStream() throws SQLException;
    public Reader getCharacterStream() throws SQLException;
    public Writer setCharacterStream() throws SQLException;
    public String getString() throws SQLException;
    public void setString(String str) throws SQLException;
    public <T extends Source> T getSource(Class<T> clazz) throws SQLException;
    public <T extends Result> T setResult(Class<T> clazz) throws SQLException;
}

