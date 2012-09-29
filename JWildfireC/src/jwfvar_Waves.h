/*
 JWildfireC - an external C-based fractal-flame-renderer for JWildfire
 Copyright (C) 2012 Andreas Maschke

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

#ifndef JWFVAR_WAVES_H_
#define JWFVAR_WAVES_H_

#include "jwf_Variation.h"

class WavesFunc: public Variation {
public:
	WavesFunc() {
	}

	const char* getName() const {
		return "waves";
	}

	void transform(FlameTransformationContext *pContext, XForm *pXForm, XYZPoint *pAffineTP, XYZPoint *pVarTP, JWF_FLOAT pAmount) {
		pVarTP->x += pAmount * (pAffineTP->x + pXForm->coeff10 * JWF_SIN(pAffineTP->y / (pXForm->coeff20 * pXForm->coeff20 + EPSILON)));
		pVarTP->y += pAmount * (pAffineTP->y + pXForm->coeff11 * JWF_SIN(pAffineTP->x / (pXForm->coeff21 * pXForm->coeff21 + EPSILON)));
		if (pContext->isPreserveZCoordinate) {
			pVarTP->z += pAmount * pAffineTP->z;
		}
	}

	WavesFunc* makeCopy() {
		return new WavesFunc(*this);
	}

};

#endif // JWFVAR_WAVES_H_
