package org.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.entities.Train;
import org.entities.User;
import org.utils.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserBookingService {
    private User user;

    private List<User> userList;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String USERS_PATH = resolveUsersPath();

    private static String resolveUsersPath() {
        String[] candidates = new String[]{
                "app/src/main/java/org/localDb/users.json",
                "src/main/java/org/localDb/users.json",
                "../app/src/main/java/org/localDb/users.json"
        };
        for (String candidate : candidates) {
            Path p = Paths.get(System.getProperty("user.dir"), candidate).normalize();
            if (Files.exists(p)) {
                return p.toString();
            }
        }
        return Paths.get("src/main/java/org/localDb/users.json").toString();
    }

    public UserBookingService(User user1) throws IOException {
        this.user=user1;
        File users=new File(USERS_PATH);
        userList=objectMapper.readValue(users, new TypeReference<List<User>>() {});

    }

    public UserBookingService() throws IOException{
        File users=new File(USERS_PATH);
        userList = objectMapper.readValue(users, new TypeReference<List<User>>(){});

    }


    public Boolean loginUser(){
        Optional<User> foundUser = userList.stream().filter(user1 ->
            user1.getName().equals(user.getName()) &&
            UserServiceUtil.checkPassword(user.getPassword(), user1.getHashpassword())
        ).findFirst();
        return foundUser.isPresent();
    }

    public Boolean signUp(User user1){
        try{
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;

        }
        catch(IOException ex){
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        File usersFile = new File(USERS_PATH);
        objectMapper.writeValue(usersFile,userList);

    }

    public void fetchBooking(){
        Optional<User> userFetched = userList.stream().filter(user1 ->
            user1.getName().equals(user.getName()) &&
            UserServiceUtil.checkPassword(user.getPassword(), user1.getHashpassword())
        ).findFirst();
        if(userFetched.isPresent()){
            userFetched.get().printTickets();
        }
    }

    public Boolean cancelBooking(String ticketId){

        Scanner s=new Scanner(System.in);
        System.out.println("Enter the ticket id to cancel");
        ticketId=s.next();

        if(ticketId==null || ticketId.isEmpty()){
            System.out.println("Ticket id can't be empty");
            return false;
        }

        String finalticketid1=ticketId;
        boolean removed=user.getTicketsBooked().removeIf(ticket->ticket.getTicketId().equals(finalticketid1));

        String finalticketId=ticketId;
        user.getTicketsBooked().removeIf(Ticket -> Ticket.getTicketId().equals(finalticketId));
        if(removed){
            System.out.println("Ticket has been cancelled");
            return true;
        }
        else{
            System.out.println("No ticket Found");
            return false;
        }
    }

    public List<Train> getTrains(String source, String destination){
        try{
            TrainService trainService=new TrainService();
            return trainService.searchTrains(source,destination);
        }
        catch(IOException e){
            return new ArrayList<>();
        }
    }

    public List<List<Integer>>fetchSeats(Train train){
        return train.getSeats();
    }


    public Boolean bookTrainSeat(Train train, int row, int seat){
        try{
            TrainService trainService=new TrainService();
            List<List<Integer>>seats=train.getSeats();
            if(row>=0 && row<seats.size() && seat>=0 && seat<seats.get(row).size()){
                if(seats.get(row).get(seat)==0){
                    seats.get(row).set(seat,1);
                    train.setSeats(seats);
                    trainService.addTrain(train);
                    return true;

                }
                else{
                    return false;
                }
            }
            else{
                return false;
            }
        }catch(Exception e){
            return false;
        }
    }





}
