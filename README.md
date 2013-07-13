# Noise Machine #1

Dear Overtone community,

Just wanted to drop a line to say thanks for being such an inspiring and helpful bunch of folks. We were asked to put together a motion sensing musical instrument this summer for the [Honolulu Museum of Art, Spalding House](http://honolulumuseum.org/11981-contemporary_museum_spalding_house)'s exhibition on music. This is the code we created for our Noise Machine #1 installation.

## Ingredients: Overtone, Bifocals, Quil

* [Overtone](http://overtone.github.io): Awesome live coding environment for making sound with SuperCollider.
* [Bifocals](https://github.com/aperiodic/bifocals): Wrapper for the SimpleOpenNI Kinect library.
* [Quil](https://github.com/quil/quil): Processing in Clojure!

See the [project.clj](https://github.com/PasDeChocolat/OBQExperiment/blob/master/project.clj) file for all dependencies.

## Thanks, and what is this?

The musical parts are done with Overtone, the motion detection via Kinect, and the visuals via Quil/Processing. The installation is placed next to a bonafide, real, wood and metal harpsichord. So, we used that connection to direct our sound generation. We found [Chris Ford](https://twitter.com/ctford)'s harpsichord code on a [Gist he put online](https://gist.github.com/ctford/2877443). Big thanks for that and all of his helpful stuff ([Leipzig](https://github.com/ctford/leipzig) and a [talk on Functional Composition](http://www.youtube.com/watch?v=Mfsnlbd-4xQ)).

Chris Ford mentioned on the [Overtone forum](https://groups.google.com/forum/#!msg/overtone/m_vfRK0gZXA/hSu-6aRwi68J) that the [harpsichord synth](https://github.com/ctford/goldberg/pull/1) that he uses is [Phil Potter](https://twitter.com/philandstuff)'s work.

Also, special shout out to [Sam Aaron](https://twitter.com/samaaron) for the awesome development tools (both for Quil and Overtone). We spent a bunch of time running through all Overtone demos and sample code. We event took a look at [Karsten Schmidt](https://twitter.com/toxi)'s great [Resonate Overtone workshop files](http://hg.postspectacular.com/resonate-2013).

We had a ton of fun putting it together and the reception has been great. People are moving around and interacting with Overtone. What's especially rewarding for us is that it gets people talking and thinking about the technology, in the context of musical instruments and expression. It's a bit of an aside, but the *real* harpsichord was commissioned to be somewhat radical for its time. Times have changed!

Videos of people interacting with the installation:
* http://instagram.com/p/bDEA72tu8U/
* http://instagram.com/p/ay9uG7tu4o/
* http://instagram.com/p/ay-JHQtu5T/

Pics of the installation:
* http://instagram.com/p/bDobPrQ0ju/
* http://instagram.com/p/bDoKz_w0jk/

The *real* harpsichord:
* http://instagram.com/p/au0Ix8w0lw/

This is what working in Emacs looks like on a wall:
* http://instagram.com/p/az3H0Fw0gv/

*Thanks to everyone!*

## Equipment
* digital projector 
* hdmi cables
* mac mini
* Kinect sensor (original model), with included power adapter

## The Code

The launching point for the application is `-main` function in [core.clj](https://github.com/PasDeChocolat/NoiseMachine/blob/master/src/grid/core.clj). You'll see that we follow the structure of a typical Quil project. A Quil sketch is created and run (in `run-sketch`), followed by a "row row row your boat" intro (in `play-intro`).

The main sketch has two interesting parts, `setup` and `draw`. We'll dive into those now.

### Setup

The setup function in defined in [setup.clj](https://github.com/PasDeChocolat/NoiseMachine/blob/master/src/grid/setup.clj). At the top, you'll see that we pull in the `grid-state` namespace, all the Clojure mutable state is defined in [the `state.clj` file](https://github.com/PasDeChocolat/NoiseMachine/blob/master/src/grid/state.clj).

The more interesting bits of state are:

* grid-state - Map keyed by [col row] vector, saving the dectection state of sector of the grid.
* grid-sensors - Star grid... sort of twinkly.
* all-pixies - List of all the randomized circles showing up to visualize sound (the circles).
* note-grid - Map (key: [col row]) containing the "notes" played in each sector of the grid.

The grid is x,y plane of detection seen by the Kinect. It's state is determined by the distance of an object (if detected) away from the Kinect.

### Draw

The drawing bits are a bit crazy. There are a few elements layered together. The visuals evolved with the sound and the (somewhat random) trajectory of the project. Here are the main components.

* grid sensors (flickering stars - denote grid and spacing), look at `draw-sensor-grid` in [draw_sensors.clj](https://github.com/PasDeChocolat/NoiseMachine/blob/master/src/grid/draw_sensors.clj).
* grid bursts (wobbly lines - motion), look at `update-display-sensor-element-at` in [draw_sensors.clj](https://github.com/PasDeChocolat/NoiseMachine/blob/master/src/grid/draw_sensors.clj)
* pixies (circles), look at `draw-all-pixies` in [pixies.clj](https://github.com/PasDeChocolat/NoiseMachine/blob/master/src/grid/pixies.clj).

For the sensor grid, check out

## Usage

* Open core.clj in buffer.
* `C-c M-j` nREPL-jack-in
* `C-c C-k` eval core buffer in REPL.
* `C-c M-n` switch REPL to core namespace.
* `C-c C-e` the run-sketch call.

## Todos
* [x] add visuals
* [x] flip screen
* [x] rows >= 36 causes offset problem!
* [ ] full screen command, or on launch
* [x] run as app
* [x] make activated sensors fade until next activation
* [x] refresh activated sensors on reactivation by increasing health
* [x] closer to wall, make sensor dots more detailed, e.g. cluster of actors
* [ ] from time to time, close-up of closest dots to wall
* [ ] from time to time, close up to nice looking grid (that cara likes)
* [ ] turn off bottom or top rows via settings
* [ ] turn on/off mouse zoom/rotate controls via keystroke

## License

<a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-sa/3.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/">Creative Commons Attribution-ShareAlike 3.0 Unported License</a>.

Copyright © 2013 Pas de Chocolat, LLC
