package pt.tecnico.distledger.namingserver.domain;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ServiceEntry {
    private String serviceName;

    private ArrayList<ServerEntry> serverEntries;


    public ServiceEntry(String serviceName, ArrayList<ServerEntry> serverEntries) {
        this.serviceName = serviceName;
        this.serverEntries = serverEntries;
    }


    public ServiceEntry(String serviceName) {
        this.serviceName = serviceName;
        this.serverEntries = new ArrayList<>();
    }


    public void addServerEntry(ServerEntry serverEntry) {
        serverEntries.add(serverEntry);
    }


    public ArrayList<String> getServerAddressesWithQualifier(String qualifier) {
        ArrayList<ServerEntry> serverEntries = getServerEntries();
        ArrayList<String> serverEntry = serverEntries.stream()
                .filter(entry -> entry.getQualifier().equals(qualifier))
                .map(entry -> entry.getAddress())
                .collect(Collectors.toCollection(ArrayList::new));
        return serverEntry;
    }


    public ArrayList<String> getAllServerAddresses() {
        ArrayList<String> serverEntry = serverEntries.stream()
                .map(entry -> entry.getAddress())
                .collect(Collectors.toCollection(ArrayList::new));
        return serverEntry;
    }


    public boolean checkIfQualifierIsUsed(String qualifier) {
        return serverEntries.stream().anyMatch(server -> server.getQualifier().equals(qualifier));
    }


    public String getServiceName() {
        return serviceName;
    }


    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }


    public ArrayList<ServerEntry> getServerEntries() {
        return this.serverEntries;
    }


    public void setServerEntries(ArrayList<ServerEntry> serverEntries) {
        this.serverEntries = serverEntries;
    }


    public void removeServerEntry(String address) {
        ArrayList<ServerEntry> newEntries = serverEntries.stream()
                .filter(serverEntry -> !serverEntry.getAddress().equals(address))
                .collect(Collectors.toCollection(ArrayList::new));
        setServerEntries(newEntries);
    }


    @Override
    public String toString() {
        return "ServiceEntry{" +
                "serviceName='" + serviceName + '\'' +
                ", serverEntries=" + serverEntries +
                '}';
    }
}