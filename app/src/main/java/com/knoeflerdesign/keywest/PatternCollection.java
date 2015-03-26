package com.knoeflerdesign.keywest;

/**
 * Created by Michael on 14.02.2015.
 */
public interface PatternCollection {
    final String SPACE = "\\s";
    //Name Conventions
    final String STANDARD_NAME_PATTERN = "[öÖäÄüÜßA-ZA-za-z]{3,20}"+SPACE+"+[öÖäÄüÜßA-Za-z]{2,40}";
    final String STANDARD_SINGLE_WORD_PATTERN ="[öÖäÄüÜßA-ZA-za-z]{3,40}";
    /*final String STANDARD_SINGLE_WORD_PATTERN_with_space_at_end = "[öÖäÄüÜßA-ZA-za-z]{3,40}"+SPACE+"";*/
    final String STANDARD_NAME_WITH_SECOND_FIRSTNAME_PATTERN = "[öÖäÄüÜßA-ZA-za-z]{3,20}"+SPACE+"+[öÖäÄüÜßA-Za-z]{3,20}"+SPACE+"+[öÖäÄüÜßA-Za-z]{2,40}";
    final String STANDARD_NAME_WITH_SECOND_LASTNAME_PATTERN = "[öÖäÄüÜßA-ZA-za-z]{3,20}"+SPACE+"+[öÖäÄüÜßA-Za-z]{2,40}+[\\-]{1}+[öÖäÄüÜßA-Za-z]{2,40}";

    //for registration of an entry
    //Format: Max Mustermann
    final String ACCEPTED_NAME_PATTERN = "[öÖäÄüÜßA-z]{3,20}"+SPACE+"+[öÖäÄüÜßA-z]{2,40}";
    //Format: Max Moritz Mustermann
    final String ACCEPTED_NAME_WITH_SECOND_FIRSTNAME_PATTERN = "[öÖäÄüÜßA-z]{3,20}+[\\-]{1}+[öÖäÄüÜßA-z]{3,20}"+SPACE+"+[öÖäÄüÜßA-z]{2,40}";
    //Format: Max Tester-Mustermann
    final String ACCEPTED_NAME_WITH_SECOND_LASTNAME_PATTERN = "[öÖäÄüÜßA-z]{3,20}"+SPACE+"+[öÖäÄüÜßA-z]{2,40}+[\\-]{1}+[öÖäÄüÜßA-z]{2,40}";
    //Format: Max Moritz Tester-Mustermann
    final String ACCEPTED_NAME_WITH_SECOND_FIRST_AND_LASTNAME_PATTERN = "[öÖäÄüÜßA-z]{3,20}+[\\-]{1}+[öÖäÄüÜßA-z]{3,20}"+SPACE+"+[öÖäÄüÜßA-z]{2,40}+[\\-]{1}+[öÖäÄüÜßA-z]{2,40}";

    final String STANDARD_AGE_PATTERN = "^[0-9]{1,3}+$";
    final String STANDARD_DATE_PATTERN = "[0-9]{2}+[.]{1}+[0-9]{2}+[.]{1}[0-9]{4}";
    final String STANDARD_DATE_PATTERN_WITHOUT_DOTS = "[0-9]{8}";


    final String FIRST_NAME_WITH_AGE = STANDARD_SINGLE_WORD_PATTERN + SPACE + STANDARD_AGE_PATTERN;
    final String FULLNAME_WITH_AGE = STANDARD_NAME_PATTERN + SPACE + STANDARD_AGE_PATTERN;

    final String[] ACCEPTED_NAME_CONVENTIONS = {
            ACCEPTED_NAME_WITH_SECOND_FIRST_AND_LASTNAME_PATTERN,
            ACCEPTED_NAME_WITH_SECOND_FIRSTNAME_PATTERN,
            ACCEPTED_NAME_WITH_SECOND_LASTNAME_PATTERN,
            ACCEPTED_NAME_PATTERN
    };
    final String[] NAME_CONVENTIONS ={
            STANDARD_NAME_PATTERN,
            /*STANDARD_MULTI_NAME_PATTERN_with_space_at_end, */
            STANDARD_NAME_WITH_SECOND_FIRSTNAME_PATTERN,
            STANDARD_NAME_WITH_SECOND_LASTNAME_PATTERN,/*
            STANDARD_SINGLE_WORD_PATTERN_with_space_at_end*/
    };
    final String[] SINGLE_NAME_CONVENTIONS = {
            STANDARD_SINGLE_WORD_PATTERN/*,
            STANDARD_SINGLE_WORD_PATTERN_with_space_at_end*/
    };
    final String[] NAME_PLUS_AGE_CONVENTIONS= {
            FIRST_NAME_WITH_AGE,
            FULLNAME_WITH_AGE
    };
    //Numbers




    //set also date and age as possible searchresults
    final String[] DATE_CONVENTIONS = {
            STANDARD_DATE_PATTERN,
            STANDARD_DATE_PATTERN_WITHOUT_DOTS
    };
    final String[] AGE_CONVENTIONS = {STANDARD_AGE_PATTERN};

}
