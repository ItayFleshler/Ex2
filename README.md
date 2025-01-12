# Ex2 - Foundation of Object-Oriented and Recursion

## Overview

The `Ex2Sheet` class is a comprehensive implementation of a spreadsheet system supporting text, numbers, and formulas. It includes advanced features such as formula parsing, cell dependency management, cycle detection, and file I/O operations. This document provides a detailed overview of the project's structure, functionality, and testing strategy.

## Image of the spreadsheet after all the implementations
![11BB9918-5B1F-43F4-83E6-F724E9FCEECB](https://github.com/user-attachments/assets/01414424-b9ef-4008-b2a0-a606068922ae)

---

## Formula Parsing and Evaluation

The `Ex2Sheet` class supports parsing and evaluating complex formulas, dividing the process into two main phases:

### 1. Parsing
- Constructs an Abstract Syntax Tree (AST) to represent the formula structure.
- Validates the syntax of the formula, ensuring proper use of operators, parentheses, and references.

#### Key Parsing Functions
- **`isNumber`:** Checks if a cell's content is a valid number using `Double.parseDouble`. Returns `true` for valid numeric values.
- **`isText`:** Determines if the content is non-numeric and not a formula, classifying it as text.
- **`isForm`:** Identifies if the cell contains a formula (starts with `=`).
- **`isBalancedParentheses`:** Ensures parentheses in the formula are correctly balanced to avoid syntax errors.

### 2. Evaluation
- Recursively evaluates the AST, resolving cell references dynamically.
- Supports nested expressions, operator precedence, and scientific notation (e.g., `1e5`).
- Includes robust error handling for invalid formulas and division by zero.

#### Key Evaluation Functions
- **`computeForm`:** Processes and evaluates formulas, handling nested expressions and operator precedence.
- **`performOperation`:** Executes basic arithmetic operations (`+`, `-`, `*`, `/`). Returns `null` for division by zero.
- **`setEvaluatedValue` / `getEvaluatedValue`:** Stores and retrieves computed values, avoiding redundant calculations.

---

## Managing Cell Dependencies and Cycles

The `Ex2Sheet` class manages dependencies to ensure correct evaluation order and handles cyclic references effectively.

### Dependency Management

#### Key Features
- **Dependency Graph:** Represents cells as nodes and their references as edges.
- **Depth Calculation:** Assigns a depth to each cell based on its dependencies. Independent cells have a depth of `0`, while dependent cells have increasing depths.
- **Cycle Detection:** Identifies and handles circular references (e.g., `A1 -> B1 -> A1`) to prevent infinite loops.

#### Dependency Functions
- **`calculateCellDepth`:** Recursively determines a cell's depth based on its dependencies. Detects and flags circular references with `ERR_CYCLE`.
- **`depth`:** Generates a 2D array representing the depth of every cell in the spreadsheet.

---

## File I/O Operations

The `Ex2Sheet` class includes robust support for saving and loading spreadsheets.

### Saving and Loading

#### Serialization
- Converts the spreadsheet's state into a string-based format suitable for file storage.
- Escapes special characters (e.g., commas, newlines) to ensure data integrity.

#### Deserialization
- Reads and reconstructs the spreadsheet from a saved file.
- Handles malformed or incomplete files gracefully to avoid corruption.

#### File Format
- **Dimensions:** The first line specifies the spreadsheet's size (e.g., `10,10`).
- **Cell Data:** Each subsequent line corresponds to a row of cells, with content separated by commas. Empty cells are represented as `EMPTY`.

#### Functions
- **`save`:** Writes the spreadsheet dimensions and content to a file.
- **`load`:** Reads and reconstructs the spreadsheet from a saved file.

---

## Testing and Validation

### Comprehensive Testing

1. **Unit Testing**
   - Tests individual methods and classes.
   - Examples:
     - Validating `isNumber`, `isText`, and `isForm`.
     - Evaluating formulas with `computeForm`.

2. **Integration Testing**
   - Ensures seamless interaction between components (`Ex2Sheet`, `SCell`, `CellEntry`).

3. **Edge Case Testing**
   - Tests unusual inputs such as invalid formulas, extremely large numbers, or blank cells.

4. **Performance Testing**
   - Evaluates responsiveness and efficiency with large spreadsheets and complex dependencies.

### Test Cases

#### Key Scenarios
- **Text Handling:** Validates classification of non-numeric, non-formula input.
- **Valid Formulas:** Tests nested expressions and operator precedence.
- **Invalid Formulas:** Identifies syntax errors, invalid operators, and missing parentheses.
- **Circular References:** Detects and flags cycles within the dependency graph.

---

## Key Features and Highlights

- **Dynamic Formula Evaluation:** Supports nested expressions, cell references, and operator precedence.
- **Error Handling:** Detects and flags invalid formulas and circular references.
- **File Persistence:** Enables saving and loading of spreadsheet states for continued work.
- **Scalable Design:** Handles large spreadsheets with thousands of cells efficiently.
