package com.ronanos.lexiconlair.dictionary;


import java.util.List;


public class DictionaryWordDTO {

    private String word;
    private String phonetic;
    private List<PhoneticDTO> phonetics;
    private String origin;
    private List<MeaningDTO> meanings;

    // Getters and Setters
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public List<PhoneticDTO> getPhonetics() {
        return phonetics;
    }

    public void setPhonetics(List<PhoneticDTO> phonetics) {
        this.phonetics = phonetics;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public List<MeaningDTO> getMeanings() {
        return meanings;
    }

    public void setMeanings(List<MeaningDTO> meanings) {
        this.meanings = meanings;
    }

    // Inner class for Phonetic DTO
    public static class PhoneticDTO {
        private String text;
        private String audio;

        // Getters and Setters
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getAudio() {
            return audio;
        }

        public void setAudio(String audio) {
            this.audio = audio;
        }
    }

    // Inner class for Meaning DTO
    public static class MeaningDTO {
        private String partOfSpeech;
        private List<DefinitionDTO> definitions;

        // Getters and Setters
        public String getPartOfSpeech() {
            return partOfSpeech;
        }

        public void setPartOfSpeech(String partOfSpeech) {
            this.partOfSpeech = partOfSpeech;
        }

        public List<DefinitionDTO> getDefinitions() {
            return definitions;
        }

        public void setDefinitions(List<DefinitionDTO> definitions) {
            this.definitions = definitions;
        }

        // Inner class for Definition DTO
        public static class DefinitionDTO {
            private String definition;
            private String example;
            private List<String> synonyms;
            private List<String> antonyms;

            // Getters and Setters
            public String getDefinition() {
                return definition;
            }

            public void setDefinition(String definition) {
                this.definition = definition;
            }

            public String getExample() {
                return example;
            }

            public void setExample(String example) {
                this.example = example;
            }

            public List<String> getSynonyms() {
                return synonyms;
            }

            public void setSynonyms(List<String> synonyms) {
                this.synonyms = synonyms;
            }

            public List<String> getAntonyms() {
                return antonyms;
            }

            public void setAntonyms(List<String> antonyms) {
                this.antonyms = antonyms;
            }
        }
    }
}
