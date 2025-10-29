package org.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.entities.Train;
import org.entities.User;
import org.services.TrainService;
import org.services.UserBookingService;
import org.utils.UserServiceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static spark.Spark.*;

public class Server {
    public static void main(String[] args) {
        port(8080);
        ObjectMapper mapper = new ObjectMapper();

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });
        options("*", (req, res) -> {
            res.status(204);
            return "";
        });

        get("/health", (req, res) -> {
            res.type("application/json");
            return "{\"ok\":true}";
        });

        post("/signup", (req, res) -> {
            res.type("application/json");
            Map<String, String> body = mapper.readValue(req.body(), Map.class);
            String name = body.getOrDefault("name", "");
            String password = body.getOrDefault("password", "");
            UserBookingService ubs = new UserBookingService();
            User user = new User(name, password, UserServiceUtil.hashPassword(password), new java.util.ArrayList<>(), java.util.UUID.randomUUID().toString());
            boolean ok = ubs.signUp(user);
            return mapper.writeValueAsString(Map.of("success", ok));
        });

        post("/login", (req, res) -> {
            res.type("application/json");
            Map<String, String> body = mapper.readValue(req.body(), Map.class);
            String name = body.getOrDefault("name", "");
            String password = body.getOrDefault("password", "");
            User user = new User(name, password, UserServiceUtil.hashPassword(password), new java.util.ArrayList<>(), "");
            UserBookingService ubs = new UserBookingService(user);
            boolean ok = ubs.loginUser();
            return mapper.writeValueAsString(Map.of("success", ok));
        });

        get("/trains", (req, res) -> {
            res.type("application/json");
            String source = req.queryParams("source");
            String dest = req.queryParams("dest");
            UserBookingService ubs = new UserBookingService();
            List<Train> trains = ubs.getTrains(source, dest);
            return mapper.writeValueAsString(trains);
        });

        get("/seats", (req, res) -> {
            res.type("application/json");
            String trainId = req.queryParams("trainId");
            TrainService ts = new TrainService();
            Optional<Train> t = ts.getTrainById(trainId);
            if (t.isEmpty()) {
                res.status(404);
                return mapper.writeValueAsString(Map.of("error", "Train not found"));
            }
            return mapper.writeValueAsString(t.get().getSeats());
        });

        post("/book", (req, res) -> {
            res.type("application/json");
            Map<String, Object> body = mapper.readValue(req.body(), Map.class);
            String trainId = (String) body.get("trainId");
            int row = (int) body.getOrDefault("row", 0);
            int col = (int) body.getOrDefault("col", 0);
            TrainService ts = new TrainService();
            Optional<Train> t = ts.getTrainById(trainId);
            if (t.isEmpty()) {
                res.status(404);
                return mapper.writeValueAsString(Map.of("success", false, "error", "Train not found"));
            }
            UserBookingService ubs = new UserBookingService();
            boolean booked = ubs.bookTrainSeat(t.get(), row, col);
            Map<String, Object> out = new HashMap<>();
            out.put("success", booked);
            return mapper.writeValueAsString(out);
        });
    }
}


