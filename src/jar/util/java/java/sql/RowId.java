package java.sql;

public interface RowId {
    public boolean equals(Object o);
    public byte[] getBytes();
    public String toString();
    public int hashCode();
}

