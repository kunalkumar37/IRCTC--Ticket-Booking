package org.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.entities.Train;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TrainService {

    private List<Train> trainList;
    private ObjectMapper objectMapper=new ObjectMapper();
    private static final String TRAIN_DB_PATH = resolveTrainsPath();

    private static String resolveTrainsPath() {
        String[] candidates = new String[]{
                "app/src/main/java/org/localDb/trains.json",
                "src/main/java/org/localDb/trains.json",
                "../app/src/main/java/org/localDb/trains.json"
        };
        for (String candidate : candidates) {
            Path p = Paths.get(System.getProperty("user.dir"), candidate).normalize();
            if (Files.exists(p)) {
                return p.toString();
            }
        }
        return Paths.get("src/main/java/org/localDb/trains.json").toString();
    }

    public TrainService() throws IOException {
        File trains=new File(TRAIN_DB_PATH);
        trainList=objectMapper.readValue(trains,new TypeReference<List<Train>>() {});

    }

    public List<Train> searchTrains(String source, String destination){
        return trainList.stream().filter(train->validTrain(train,source,destination)).collect(Collectors.toList());

    }

    public void addTrain(Train newTrain){
        Optional<Train> existingTrain=trainList.stream().filter(train->train.getTrainId().equalsIgnoreCase(newTrain.getTrainId()))
                .findFirst();
        if(existingTrain.isPresent()){
            updateTrain(newTrain);
        }
        else{
            trainList.add(newTrain);
            saveTrainListToFile();
        }

    }

    public void updateTrain(Train updatedTrain){
        OptionalInt index= IntStream.range(0,trainList.size())
                .filter(i->trainList.get(i).getTrainId().equalsIgnoreCase(updatedTrain.getTrainId()))
                .findFirst();
        if(index.isPresent()){
            trainList.set(index.getAsInt(),updatedTrain);
            saveTrainListToFile();
        }
        else{
            addTrain(updatedTrain);
        }
    }

    public void saveTrainListToFile(){
        try{
            objectMapper.writeValue(new File(TRAIN_DB_PATH), trainList);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public Optional<Train> getTrainById(String trainId){
        return trainList.stream().filter(t -> t.getTrainId().equalsIgnoreCase(trainId)).findFirst();
    }


    private boolean validTrain(Train train, String source, String destination){
        List<String> stationOrder=train.getStations();
        int sourceIndex=stationOrder.indexOf(source.toLowerCase());
        int destinationIndex=stationOrder.indexOf(destination.toLowerCase());
        return sourceIndex!=-1 && destinationIndex!=-1 && sourceIndex < destinationIndex;
    }
}
