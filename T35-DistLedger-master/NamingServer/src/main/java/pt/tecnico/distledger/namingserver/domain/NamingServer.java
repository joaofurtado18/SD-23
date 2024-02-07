package pt.tecnico.distledger.namingserver.domain;

import pt.tecnico.distledger.server.exceptions.NotPossibleToRegisterServerException;
import pt.tecnico.distledger.server.exceptions.NotPossibleToRemoveServerException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NamingServer {

    private ConcurrentHashMap<String, ServiceEntry> serviceEntries;

    private final String DIST_LEDGER_SERVICE = "DistLedger";


    private void addServiceEntry(String service, String qualifier, String address) {
        ServerEntry serverEntry = new ServerEntry(qualifier, address);
        ServiceEntry serviceEntry = new ServiceEntry(service);
        serviceEntry.addServerEntry(serverEntry);
        if (!serviceEntries.containsKey(service))
            serviceEntries.put(service, serviceEntry);
        else
            serviceEntries.get(service).addServerEntry(serverEntry);
    }


    public NamingServer() {
        serviceEntries = new ConcurrentHashMap<String, ServiceEntry>();
        serviceEntries.put(DIST_LEDGER_SERVICE, new ServiceEntry(DIST_LEDGER_SERVICE));
    }


    public NamingServer(ConcurrentHashMap<String, ServiceEntry> serviceEntries) {
        this.serviceEntries = serviceEntries;
    }


    public ConcurrentHashMap<String, ServiceEntry> getServiceEntries() {
        return serviceEntries;
    }


    public void setServiceEntries(ConcurrentHashMap<String, ServiceEntry> serviceEntries) {
        this.serviceEntries = serviceEntries;
    }


    private boolean checkIfAddressIsUsed(String service, String address) {
        return serviceEntries.get(service).getAllServerAddresses().contains(address);
    }


    private boolean checkIfQualifierIsUsed(String service, String qualifier) {
        return serviceEntries.get(service).checkIfQualifierIsUsed(qualifier);
    }


    public void register(String service, String qualifier, String address) throws NotPossibleToRegisterServerException {
        if (checkIfAddressIsUsed(service, address) || checkIfQualifierIsUsed(service, qualifier))
            throw new NotPossibleToRegisterServerException();

        addServiceEntry(service, qualifier, address);
        System.out.println(String.format("Registered server with address %s, qualifier %s in service %s", address, qualifier, service));
    }


    public ArrayList<String> lookup(String service, String qualifier) {
        ConcurrentHashMap<String, ServiceEntry> serviceEntries = getServiceEntries();
        if (!serviceEntries.containsKey(service))
            return new ArrayList<String>();
        ServiceEntry entry = serviceEntries.get(service);

        return !qualifier.isEmpty() && qualifier != null ?
                entry.getServerAddressesWithQualifier(qualifier) :
                entry.getAllServerAddresses();
    }


    public void delete(String service, String address) throws NotPossibleToRemoveServerException {
        if (!serviceEntries.get(service).getAllServerAddresses().contains(address))
            throw new NotPossibleToRemoveServerException();

        ArrayList<ServerEntry> serverEntries = serviceEntries.get(service).getServerEntries();
        for (ServerEntry s : serverEntries) {
            if (s.getAddress().equals(address)) {
                serviceEntries.get(service).removeServerEntry(address);
                System.out.println(String.format("Deleted server with address %s in service %s", address, service));
            }
        }
    }


    @java.lang.Override
    public java.lang.String toString() {
        return "NamingServer{" +
                "serviceEntries=" + serviceEntries +
                '}';
    }
}
