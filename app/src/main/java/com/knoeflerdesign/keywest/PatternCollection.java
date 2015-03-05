package com.knoeflerdesign.keywest;

/**
 * Created by Michael on 14.02.2015.
 */
public interface PatternCollection {

    //Name Conventions
    final String STANDARD_NAME_PATTERN = "[öÖäÄüÜßA-Za-z]{3,20}\\s+[öÖäÄüÜßA-Za-z]{2,40}";
    final String STANDARD_SINGLE_WORD_PATTERN ="[öÖäÄüÜßA-Za-z]{3,40}\\s??";
    //final String STANDARD_SINGLE_WORD_PATTERN_with_space_at_end = "[öÖäÄüÜßA-Za-z]{3,40}\\s";
    final String STANDARD_NAME_WITH_SECOND_FIRSTNAME_PATTERN = "[öÖäÄüÜßA-Za-z]{3,20}\\s+[öÖäÄüÜßA-Za-z]{3,20}\\s+[öÖäÄüÜßA-Za-z]{2,40}";
    final String STANDARD_NAME_WITH_SECOND_LASTNAME_PATTERN = "[öÖäÄüÜßA-Za-z]{3,20}\\s+[öÖäÄüÜßA-Za-z]{2,40}+[\\-]{1}+[öÖäÄüÜßA-Za-z]{2,40}";
    final String STANDARD_MULTI_NAME_PATTERN_with_space_at_end = "[öÖäÄüÜßA-Za-z]{3,20}\\s+[öÖäÄüÜßA-Za-z]{2,40}\\s";

    final String[] NAME_CONVENTIONS ={
            STANDARD_NAME_PATTERN,
            STANDARD_MULTI_NAME_PATTERN_with_space_at_end,
            STANDARD_NAME_WITH_SECOND_FIRSTNAME_PATTERN,
            STANDARD_NAME_WITH_SECOND_LASTNAME_PATTERN
    };
    final String[] SINGLE_NAME_CONVENTIONS = {
            STANDARD_SINGLE_WORD_PATTERN/*,
            STANDARD_SINGLE_WORD_PATTERN_with_space_at_end*/
    };
    //Numbers

    final String STANDARD_AGE_PATTERN = "^[0-9]{1,3}+$";
    final String STANDARD_DATE_PATTERN = "[0-9]{2}+[.]{1}+[0-9]{2}+[.]{1}[0-9]{4}";


    //set also date and age as possible searchresults
    final String[] DATE_CONVENTIONS = {STANDARD_DATE_PATTERN};
    final String[] AGE_CONVENTIONS = {STANDARD_AGE_PATTERN};

}
