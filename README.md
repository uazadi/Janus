# Janus

Janus is an Eclipse plug-in that aims to automate the code clones detection and refactoring through an iterative workflow, depicted in the figure below.

<p align="center">
  <img src="https://github.com/uazadi/Janus/blob/master/icons/JanusFlow.png" width="700">
</p>

* *Source code pre-processing*: the source code is loaded in memory, normalised and filtered based on the user needs;
* *Refactoring-Aware Detection of code clones*: the duplicate code detection has been model as a not costrained optimization problem, which is composed by two main functions. The Similarity function that detect the cloned statements and it quantifies some feature of the duplicate code. The Refactoring Risk that aim to estimate the effort requested to accomplish the refactoring. 
* *Code Clones Refactoring*: Janus is currently able to apply automatically three type of refactoring techiques that remove automatically cloned statements Extract Method, Extract and Pull-Up Method and Extract Superclass.
* *Behavioural Checks*: After the refactoring Janus is able to automatically compile the project and lunch the classes contain a _main_ method and the JUnit test cases selected by the User. Furthermore, if even one of the fails Janus is able texecute automatically the fallback since the entire refactoring process is subjected to _versioning via Git_.

Below the Stack diagram of Janus’s architecture is provided

<p align="center">
  <img src="https://github.com/uazadi/Janus/blob/master/icons/StackDiagram.png" width="700">
</p>

### Information 
If you would like more information about this tool:
* read my [Master thesis](https://www.researchgate.net/publication/340932700_Automation_of_duplicate_code_detection_and_refactoring)
* check out the [presentation](https://www.slideshare.net/UmbertoAzadi/janus-automation-of-duplicate-code-detection-and-refactoring)

### Problems
For report any problem please check if already exist an [Issue](https://github.com/UmbertoAzadi/Janus/issues) on GitHub about it:
* if there isn't one please add an Issue
* if there is one please leave a comment 
