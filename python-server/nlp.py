    #!/usr/bin/python
import sys
import nltk, re, pprint
from nltk.corpus import stopwords
from nltk.parse.stanford import StanfordDependencyParser
from nltk.tokenize.moses import MosesDetokenizer
import os
import time
from nltk.translate.bleu_score import sentence_bleu


class NLP:
    path = os.path.dirname(os.path.realpath(__file__))
    pathToLibs = os.path.dirname(os.path.realpath(__file__)) + "/../src/libs/"
    pathToJar = pathToLibs + "stanford-corenlp-3.8.0.jar"
    pathToModels = pathToLibs + "stanford-corenlp-3.8.0-models.jar"
    dependency_parser = StanfordDependencyParser(path_to_jar=pathToJar, path_to_models_jar=pathToModels)
    pathToCommands = path + "/pythonCommands.csv"
    pathToKeywords = path + "/keywords.csv"
    commands = {}
    keywords = {}
    actions = []
    targets = []
    textTriples = []
    tagged = ""
    stopWords = []
    finalResult = {}
    
    # Only Simple Sentences
    
    def __init__(self):
        self.setUp()
    
    def reset(self):
        self.textTriples = []
        self.tagged = ""
        self.finalResult = {}
     
    def tagKeywords(self, sentence):
        newSentence = []
        for (word, tag) in sentence:
            newTag = tag
            if word in self.actions:
               newTag = "VB"
            elif word in self.targets:
                newTag = "NN"   
            elif '.' in word and word is not '.':
                    newTag = "NNP"
            else:
                try: 
                    int(word)
                    newTag = "NNP"
                except:
                    None
            newSentence.append((word, newTag))
        return newSentence    
    
    def createKeywordThesaurus(self):
        file = open(self.pathToKeywords, "r");
        lines = iter(file)
        akeys = set()
        tkeys = set()
        for line in lines:
            line = line.strip('\n')
            splitLine = line.split(",")
            type = splitLine.pop(0)
            key = splitLine.pop(0)
        
            if (type == "0"): 
                self.actions.extend(splitLine)
                akeys.add(key)
            else:
                self.targets.append(splitLine)
                tkeys.add(key)
            self.keywords[key] = splitLine;
        
        for k in akeys:
            self.actions.insert(0, k)
            self.commands[k] = []   
        
        for k in tkeys:
            self.targets.insert(0, k)
            
            
    def setUp(self):
        self.fillStopWords()
        self.createKeywordThesaurus()
        self.loadCommands()
    
    def fillStopWords(self):
        extras = ['i', "want", 'like', 'would', 'with']
        self.stopWords = set(stopwords.words("english")).union(extras)
        self.stopWords.remove("all")
    
    def loadCommands(self):
        file = open(self.pathToCommands, "r");
        lines = iter(file)
        next(lines)
        options = []
        triples = []
        toAdd = None
        currentCommand = {}
        for line in lines:
            splitLine = line.split(",")
            type = splitLine.pop(0)
            if (type == "Command"):
                if (currentCommand <> {}) :
                    adds = toAdd.split("|")
                    for add in adds:
                        list = self.commands[add]
                        list.append(currentCommand)
                        self.commands.update({add: list})
                    triples = []
                    currentCommand = {}
                currentCommand["Name"] = splitLine[0]
                sentences = nltk.sent_tokenize(splitLine[1].lower())
                currentCommand["Triples"] = []
                for sent in sentences:
                    currentCommand["Triples"].extend(self.getTriples(sent,printTriples=True))
                moreTrips = []
                for trip in currentCommand["Triples"]:
                    key = self.getSynonym(trip[0][0])
                    if key is not None:
                        newTup = (unicode(key),trip[0][1])
                        newTrip = (newTup,trip[1],trip[2])
                        moreTrips.append(newTrip)
                currentCommand["Triples"].extend(moreTrips)
                currentCommand["Inputs"] = splitLine[2]
                currentCommand["Target"] = splitLine[4]
                toAdd = splitLine[3]
        # Add final command
        adds = toAdd.split("|")
        for add in adds:
            list = self.commands[add]
            list.append(currentCommand)
            self.commands.update({add: list})
     
    
    def processSentence(self, text, lem=nltk.WordNetLemmatizer()):
        filtered = []
        tokenizedSentence = nltk.word_tokenize(text)
        for word in tokenizedSentence:
            if word.lower() not in self.stopWords:
                word = lem.lemmatize(word)
                filtered.append(word)        
        if (filtered == []):
            return []
        text = nltk.pos_tag(filtered)
        return self.tagKeywords(text)
        
    def extract(self, text):
        sentences = nltk.sent_tokenize(text.lower())
        self.reset()
        if not sentences == []:
            val = self.parseSentence(sentences[0])
            if (val == None):
                return
            return self.finalResultToString()
        else:
            return

    def parseSentence(self, sentence):
        val = self.getTriples(sentence, tag=True,printTriples=True)
       
        self.textTriples.extend(val)
        return self.buildCommand(self.textTriples)
        
    def buildCommand(self, triples):
        result = None
        commandName = ""
        options = []
        optionsString = ""
        input = ""
        for trip in triples:
            newTrip = trip
            if (trip[1] == "dobj"):
                key = self.getSynonym(trip[0][0])
                if key is not None:
                    newTup = (unicode(key),trip[0][1])
                    newTrip = (newTup,trip[1],trip[2])
                command = self.selectCommand(key, newTrip)
                if command == None:
                    return
                commandName = command["Name"]
                
                self.getNNPs(self.tagged)
        result = commandName + optionsString + input
        return result
      
    def selectCommand(self, type, trip):
        keys = set(self.commands.keys())
        if type not in keys:
            return None
        list = self.commands[type]
        for command in list:
            if trip in command["Triples"]:
                self.finalResult["Command"] = command["Name"]
                return command
        return None
        
    
    def getNNPs(self, sentence):
        self.finalResult["Inputs"] = []
        for (word,tag) in sentence:
            if (tag == "NNP"):
                self.finalResult["Inputs"].append(word)

    def getSynonym(self, word):
        keys = self.keywords.keys()
        if word in keys:
            return word
        for key in keys:
            if word in self.keywords[key]:
                return key
        return None
    
    def finalResultToString(self): 
        result = self.finalResult["Command"]
        for arg in self.finalResult["Inputs"]:
            result += " " + arg
        return result

  
    def getTriples(self, text, tag = False, printTriples=False):
        # Remove all the Stopwords from the sentence
        original = text
        text = self.processSentence(text)
        if (tag):
            self.tagged = text
        if (not text):
            return None
        result = self.dependency_parser.tagged_parse(text)
        dep = next(result)
        triples = []
        for trip in dep.triples():
            triples.append(trip)
        return triples    
    
