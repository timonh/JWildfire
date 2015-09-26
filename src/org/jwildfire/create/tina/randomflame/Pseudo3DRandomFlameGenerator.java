/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2015 Andreas Maschke

  This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser 
  General Public License as published by the Free Software Foundation; either version 2.1 of the 
  License, or (at your option) any later version.
 
  This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this software; 
  if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jwildfire.create.tina.randomflame;

import java.util.ArrayList;
import java.util.List;

import org.jwildfire.base.Prefs;
import org.jwildfire.create.tina.base.Flame;
import org.jwildfire.create.tina.base.Shading;
import org.jwildfire.create.tina.randomgradient.RandomGradientGenerator;

public class Pseudo3DRandomFlameGenerator extends RandomFlameGenerator {
  private static List<RandomFlameGenerator> generators;

  static {
    generators = new ArrayList<RandomFlameGenerator>();
    generators.add(new BrokatRandomFlameGenerator());
    generators.add(new Brokat3DRandomFlameGenerator());
    generators.add(new BubblesRandomFlameGenerator());
    generators.add(new DualityRandomFlameGenerator());
    generators.add(new Bubbles3DRandomFlameGenerator());
    generators.add(new CrossRandomFlameGenerator());
    generators.add(new DualityRandomFlameGenerator());
    generators.add(new DuckiesRandomFlameGenerator());
    generators.add(new ExperimentalBubbles3DRandomFlameGenerator());
    generators.add(new ExperimentalGnarlRandomFlameGenerator());
    generators.add(new ExperimentalSimpleRandomFlameGenerator());
    generators.add(new FilledFlowers3DRandomFlameGenerator());
    generators.add(new Flowers3DRandomFlameGenerator());
    generators.add(new GnarlRandomFlameGenerator());
    generators.add(new DualityRandomFlameGenerator());
    generators.add(new Gnarl3DRandomFlameGenerator());
    generators.add(new JulianDiscRandomFlameGenerator());
    generators.add(new JuliansRandomFlameGenerator());
    generators.add(new BlackAndWhiteRandomFlameGenerator());
    generators.add(new LayerzRandomFlameGenerator());
    generators.add(new LinearRandomFlameGenerator());
    generators.add(new MandelbrotRandomFlameGenerator());
    generators.add(new RaysRandomFlameGenerator());
    generators.add(new SimpleRandomFlameGenerator());
    generators.add(new SimpleTilingRandomFlameGenerator());
    generators.add(new SierpinskyRandomFlameGenerator());
    generators.add(new DualityRandomFlameGenerator());
    generators.add(new SphericalRandomFlameGenerator());
    generators.add(new Spherical3DRandomFlameGenerator());
    generators.add(new GhostsRandomFlameGenerator());
    generators.add(new SpiralsRandomFlameGenerator());
    generators.add(new Spirals3DRandomFlameGenerator());
    generators.add(new SplitsRandomFlameGenerator());
    generators.add(new SubFlameRandomFlameGenerator());
    generators.add(new SynthRandomFlameGenerator());
    generators.add(new TentacleRandomFlameGenerator());
    generators.add(new TileBallRandomFlameGenerator());
    generators.add(new DualityRandomFlameGenerator());
    generators.add(new XenomorphRandomFlameGenerator());
  }

  private static final String RANDGEN = "RANDGEN";

  @Override
  public RandomFlameGeneratorState initState(Prefs pPrefs, RandomGradientGenerator pRandomGradientGenerator) {
    RandomFlameGeneratorState state = super.initState(pPrefs, pRandomGradientGenerator);
    RandomFlameGenerator generator = generators.get((int) (Math.random() * generators.size()));
    state.getParams().put(RANDGEN, generator);
    return state;
  }

  @Override
  public Flame prepareFlame(RandomFlameGeneratorState pState) {
    RandomFlameGenerator generator = createRandGen(pState);
    RandomFlameGeneratorState subState = generator.initState(pState.getPrefs(), pState.getGradientGenerator());
    Flame flame = generator.prepareFlame(subState);
    flame.setName(getName() + " - " + flame.hashCode());
    return flame;
  }

  private RandomFlameGenerator createRandGen(RandomFlameGeneratorState pState) {
    RandomFlameGenerator generator = (RandomFlameGenerator) pState.getParams().get(RANDGEN);
    return generator;
  }

  @Override
  public String getName() {
    return "Pseudo3D";
  }

  @Override
  public boolean isUseFilter(RandomFlameGeneratorState pState) {
    return false;
  }

  @Override
  protected Flame postProcessFlame(RandomFlameGeneratorState pState, Flame pFlame) {
    pFlame.getShadingInfo().setShading(Shading.PSEUDO3D);
    pFlame.getShadingInfo().setAmbient(0.05 + Math.random() * 0.15);
    pFlame.getShadingInfo().setDiffuse(0.40 + Math.random() * 0.60);
    pFlame.getShadingInfo().setPhong(Math.random());
    pFlame.getShadingInfo().setPhongSize(3.0 + Math.random() * 25.0);
    int lightCount = (int) (Math.random() * 4);
    if (lightCount < 2) {
      lightCount = 2;
    }
    else if (lightCount > 4) {
      lightCount = 4;
    }

    for (int i = 0; i < lightCount; i++) {
      pFlame.getShadingInfo().setLightPosX(i, 0.5 - Math.random());
      pFlame.getShadingInfo().setLightPosY(i, 0.5 - Math.random());
      pFlame.getShadingInfo().setLightPosZ(i, -0.1 - Math.random() * 20.0);
      pFlame.getShadingInfo().setLightRed(i, rndColor());
      pFlame.getShadingInfo().setLightGreen(i, rndColor());
      pFlame.getShadingInfo().setLightBlue(i, rndColor());
    }
    pFlame.setBGTransparency(false);
    return pFlame;
  }

  private int rndColor() {
    return Math.random() > 0.5 ? 0 : (int) (Math.random() * 128) + (int) (Math.random() * 127);
  }
}