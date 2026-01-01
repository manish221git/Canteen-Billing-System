package com.canteen.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generic utility class demonstrating Generics usage
 * Provides utility methods for collections
 */
public class CollectionUtils<T> {
    
    /**
     * Generic method to filter a list based on a predicate
     */
    public static <T> List<T> filter(List<T> list, java.util.function.Predicate<T> predicate) {
        return list.stream()
                  .filter(predicate)
                  .collect(Collectors.toList());
    }
    
    /**
     * Generic method to find an element in a list
     */
    public static <T> Optional<T> find(List<T> list, java.util.function.Predicate<T> predicate) {
        return list.stream()
                  .filter(predicate)
                  .findFirst();
    }
    
    /**
     * Generic method to sort a list
     */
    public static <T extends Comparable<T>> List<T> sort(List<T> list, boolean ascending) {
        List<T> sorted = new ArrayList<>(list);
        if (ascending) {
            Collections.sort(sorted);
        } else {
            sorted.sort(Collections.reverseOrder());
        }
        return sorted;
    }
    
    /**
     * Generic method to convert list to map
     */
    public static <K, V> Map<K, V> toMap(List<V> list, java.util.function.Function<V, K> keyExtractor) {
        Map<K, V> map = new HashMap<>();
        for (V item : list) {
            map.put(keyExtractor.apply(item), item);
        }
        return map;
    }
    
    /**
     * Generic method to partition a list
     */
    public static <T> Map<Boolean, List<T>> partition(List<T> list, 
                                                       java.util.function.Predicate<T> predicate) {
        return list.stream()
                  .collect(Collectors.partitioningBy(predicate));
    }
    
    /**
     * Generic method to group by key
     */
    public static <K, V> Map<K, List<V>> groupBy(List<V> list, 
                                                  java.util.function.Function<V, K> classifier) {
        return list.stream()
                  .collect(Collectors.groupingBy(classifier));
    }
}
