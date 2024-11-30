package com.example.digitalplatform.core;

import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.TeacherInfo;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class BranchAndBound implements GeneratorDecision {


    @Override
    public List<Request> execute(List<Request> requests, TeacherInfo user) {
        int capacity = user.getLimitHours();
        List<Request> items = new ArrayList<>(requests);
        items.sort((a, b) -> Double.compare(b.getRating() / b.getTime(), a.getRating() / a.getTime()));

        Queue<Node> queue = new LinkedList<>();
        Node root = new Node(0, 0, 0, calculateBound(new Node(0, 0, 0, 0, new ArrayList<>()), capacity, items), new ArrayList<>());
        queue.offer(root);

        double maxValue = 0;
        List<Request> bestItems = new ArrayList<>();

        while (!queue.isEmpty()) {
            Node currentNode = queue.poll();

            if (currentNode.level == items.size() || currentNode.bound <= maxValue) {
                continue;
            }

            // Узел с включением следующего предмета
            Request currentItem = items.get(currentNode.level);
            if (currentNode.weight + currentItem.getTime() <= capacity) {
                Node withItem = new Node(
                        currentNode.level + 1,
                        currentNode.weight + currentItem.getTime(),
                        currentNode.value + currentItem.getRating(),
                        0,
                        currentNode.itemsInKnapsack
                );
                withItem.itemsInKnapsack.add(currentItem);
                withItem.bound = calculateBound(withItem, capacity, items);

                if (withItem.value > maxValue) {
                    maxValue = withItem.value;
                    bestItems = withItem.itemsInKnapsack;
                }
                if (withItem.bound > maxValue) {
                    queue.offer(withItem);
                }
            }

            // Узел без включения следующего предмета
            Node withoutItem = new Node(
                    currentNode.level + 1,
                    currentNode.weight,
                    currentNode.value,
                    calculateBound(currentNode, capacity, items),
                    currentNode.itemsInKnapsack
            );
            if (withoutItem.bound > maxValue) {
                queue.offer(withoutItem);
            }
        }
        return bestItems;
    }

    @Override
    public GeneratorType generatorType() {
        return GeneratorType.BRANCH_AND_BOUND;
    }

    // Узел дерева решений
    static class Node {
        int level, weight;
        double value, bound;
        List<Request> itemsInKnapsack;

        Node(int level, int weight, double value, double bound, List<Request> itemsInKnapsack) {
            this.level = level;
            this.weight = weight;
            this.value = value;
            this.bound = bound;
            this.itemsInKnapsack = new ArrayList<>(itemsInKnapsack);
        }
    }

    double calculateBound(Node node, int capacity, List<Request> items) {
        if (node.weight >= capacity) return 0;
        double bound = node.value;
        int totalWeight = node.weight;
        for (int i = node.level; i < items.size(); i++) {
            if (totalWeight + items.get(i).getTime() <= capacity) {
                totalWeight += items.get(i).getTime();
                bound += items.get(i).getRating();
            } else {
                bound += (capacity - totalWeight) * items.get(i).getRating() / items.get(i).getTime();
                break;
            }
        }
        return bound;
    }
}
