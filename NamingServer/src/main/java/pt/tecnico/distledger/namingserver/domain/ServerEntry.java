package pt.tecnico.distledger.namingserver.domain;

public class ServerEntry {
    private String qualifier;

    private String address;

    public ServerEntry(String qualifier, String address) {
        this.qualifier = qualifier;
        this.address = address;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHost() {
        return address.split(":")[1];
    }

    public int getPort(){
        return Integer.parseInt(address.split(":")[0]);
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "ServerEntry{" +
                "qualifier='" + qualifier + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}