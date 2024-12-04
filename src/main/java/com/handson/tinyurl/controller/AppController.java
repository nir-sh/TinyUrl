package com.handson.tinyurl.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handson.tinyurl.model.NewTinyRequest;
import com.handson.tinyurl.model.User;
import com.handson.tinyurl.model.UserClick;
import com.handson.tinyurl.model.UserClickOut;
import com.handson.tinyurl.repository.UserClickRepository;
import com.handson.tinyurl.repository.UserRepository;
import com.handson.tinyurl.service.Redis;
import com.handson.tinyurl.service.TinyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.handson.tinyurl.model.User.UserBuilder.anUser;
import static com.handson.tinyurl.model.UserClick.UserClickBuilder.anUserClick;
import static com.handson.tinyurl.model.UserClickKey.UserClickKeyBuilder.anUserClickKey;
import static com.handson.tinyurl.util.Dates.getCurMonth;
import static org.springframework.data.util.StreamUtils.createStreamFromIterator;

@RestController
public class AppController {

    private static final int MAX_TRIES = 4;

    @Value("${base.url}")
    String baseUrl;

    @Autowired
    Redis redisService;
    @Autowired
    TinyService tinyService;
    @Autowired
    ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserClickRepository userClickRepository;
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public User createUser(@RequestParam String name) {
        User user = anUser().withName(name).build();
        user = userRepository.insert(user);
        return user;
    }

    @RequestMapping(value = "/user/{name}", method = RequestMethod.GET)
    public User getUser(@RequestParam String name) {
        User user = userRepository.findFirstByName(name);
        return user;
    }

    private void incrementMongoField(String userName, String key){
        Query query = Query.query(Criteria.where("name").is(userName));
        Update update = new Update().inc(key, 1);
        mongoTemplate.updateFirst(query, update, "users");
    }

    @RequestMapping(value = "/tiny", method = RequestMethod.POST)
            public String generate(@RequestBody NewTinyRequest request) throws JsonProcessingException {
        String tinyCode = tinyService.generateTinyCode();

        int i = 0;
        while(!redisService.set(tinyCode, om.writeValueAsString(request)) && i < MAX_TRIES){
            tinyCode = tinyService.generateTinyCode();
            ++i;
        }
        if(i == MAX_TRIES) throw new RuntimeException("Space is full");

        return baseUrl + tinyCode + "/";
    }

    @RequestMapping(value = "/{tiny}/", method = RequestMethod.GET)
    public ModelAndView getTiny(@PathVariable String tiny) throws JsonProcessingException {
        Object tinyRequestStr = redisService.get(tiny);
        NewTinyRequest tinyRequest = om.readValue(tinyRequestStr.toString(),NewTinyRequest.class);
        if (tinyRequest.getLongUrl() != null) {
            String userName = tinyRequest.getUserName();
            if ( userName != null) {
                incrementMongoField(userName, "allUrlClicks");
                incrementMongoField(userName,
                        "shorts."  + tiny + ".clicks." + getCurMonth());
                userClickRepository.save(anUserClick().userClickKey(anUserClickKey().withUserName(userName).withClickTime(new Date()).build())
                        .tiny(tiny).longUrl(tinyRequest.getLongUrl()).build());
            }
            return new ModelAndView("redirect:" + tinyRequest.getLongUrl());
        }
        else {
            throw new RuntimeException(tiny + " not found");
        }
    }

    @RequestMapping(value = "/user/{name}/clicks", method = RequestMethod.GET)
    public List<UserClickOut> getUserClicks(@RequestParam String name) {
        var userClicks = createStreamFromIterator( userClickRepository.findByUserName(name).iterator())
                .map(userClick -> UserClickOut.of(userClick))
                .collect(Collectors.toList());
        return userClicks;
    }


}