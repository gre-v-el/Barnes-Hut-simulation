# Barnes–Hut simulator
 Java implementation of [Barnes-Hut Simulation](https://en.wikipedia.org/wiki/Barnes–Hut_simulation).

 In short, it uses an [OcTree](https://en.wikipedia.org/wiki/Octree) to store all the masses in simulation and estimate the forces.

  <p style="text-align: center;">
	<img alt="two galaxies" src="images/two-galaxies.png" width=400/>
	<img alt="two galaxies" src="images/two-galaxies-debug.png" width=400/>
	<img alt="two galaxies" src="images/noise.png" width=400/>
	<img alt="two galaxies" src="images/collision.png" width=400/>
  </p>
  
 ## Features
* blender-like orbit controls
* toolbar
* time and iteration count controls
* debug view

## Technologies
* Java
* lwjgl
* ImGUI
* GLSL
* Multithreading
