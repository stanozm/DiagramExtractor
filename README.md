DiagramExtractor
================

Extractor for diagrams from the Visual Paradigm pdf reports

Usage
---------------

java -jar DiagramExtractor.jar path_to_dir_with_reports DIAGRAM_OPTIONS

where DIAGRAM_OPTIONS is one or more Numbers (separated by space) according to the list:

1 - Use Case Diagram

2 - Activity Diagram

3 - Class Diagram

4 - State Machine Diagram

5 - Entity Relationship Diagram

6 - Communication Diagram

7 - Sequence Diagram

8 - Deployment Diagram

9 - Package Diagram


The output pdf file is created in the DiagramExtractor.jar file directory.

This utility also requires the included Apache pdfbox library. The whole bundle can be downloaded from https://drive.google.com/file/d/0B4kAt0BgOp7aTF8zYWIyN2tJemM/view?usp=sharing

The input diretory with reports should contain only the pdf report files and nothing else.
