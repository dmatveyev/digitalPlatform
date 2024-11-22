package com.example.digitalplatform.core;

import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.User;

import java.util.List;

public interface GeneratorDessisions {
    List<Request> execute(List<Request> requests, User user);
}
