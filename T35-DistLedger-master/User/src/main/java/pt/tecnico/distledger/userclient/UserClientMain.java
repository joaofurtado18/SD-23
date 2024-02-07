package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.grpc.UserService;

public class UserClientMain {
    public static void main(String[] args) {
        CommandParser parser = new CommandParser(new UserService());
        parser.parseInput();

    }
}
