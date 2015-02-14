package com.knoeflerdesign.keywest;

/**
 * Created by Michael on 14.02.2015.
 */
public interface PatternCollection {
    final String STANDARD_NAME_PATTERN = "[öÖäÄüÜßA-Za-z]{3,20}\\s+[öÖäÄüÜßA-Za-z]{2,40}";
    final String STANDARD_NAME_PATTERN_with_space_at_end = "[öÖäÄüÜßA-Za-z]{3,20}\\s+[öÖäÄüÜßA-Za-z]{2,40}\\s";

    final String[] NAME_CONVENTIONS ={
            STANDARD_NAME_PATTERN,
            STANDARD_NAME_PATTERN_with_space_at_end
    };

}
