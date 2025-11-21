# Submission for CPSC210 final project

## Project Description  

**What will the application do?**
This application will allow users to input a scalar field (xy function wrt z) and visualize a general gradient descent algorithm in a 3D space. I implemented a camera controller after taking MATH 200 (Multivariable Calculus) and multithreading (simple concurrency) after taking CPSC 313 (Computer Hardware and Operating Systems).

![alt text](data/image/gd_kd.png)

**Who will use it?**
I mainly created this application as a tool to help students visualize gradient descent in a gamified simulation. 

**Why is this project of interest to you?**
While taking multivariable calculus, I realized that much of deep learning is just multiple variables with a little twist. Since I took CPSC213 (Computer Systems) over the summer, I also gained a lot of appreciation for DiskIO manipulation and concurrency. For this project, I wanted to compile all my knowledge and apply it into this 3D rendering engine with deep learning visualization. I also gained a lot of inspiration from my friend @NathanielHawron who created a similar but much more complex rendering engine in OpenGL and Vulkan that has modeling capability (something I have been considering to implement in my Java rendering engine).

This project is part of one of my larger projects which is a gamified Deep Learning bootcamp with highlight on 3D simulations, graphics, for all the operations applied in Deep Learning. Eventually, I will be displaying graphical matrices and how convolution, transformations, etc look like in 3D. 

**Key elements of the project include:**  
- 3D rendering engine
    - Vector transformations (3D -> 2D -> projection -> rasterization)
    - Concurrency locks for 

---

## User Stories  

- As a user, I want to see how convolution works mathematically with good documentation
- As a user, I want to see how an activation frame works mathematically with good documentation
- As a user, I want to see whether or not the model guesses my uploaded image correctly
- As a user, I want to clone the repo myself and resume training progress from whether it last left off
- As a user, I want to see a graph on whether or not the accuracy for predicting images is increasing or not
- As a user, I want to see the effect of convolution on an image that I provide the program with (GUI)
- As a user, I want to be able to input my own paths to my images for the convolution functions to apply their math to
- As a user, I want to be able to learn more about the project just by looking through the code and seeing good documentation

Phase1: Successfully completed stories 1, 6, 7, and 8