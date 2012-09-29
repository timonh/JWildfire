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
#ifndef JWFVAR_ESCHER_H_
#define JWFVAR_ESCHER_H_

#include "jwf_Constants.h"
#include "jwf_Variation.h"

class EscherFunc: public Variation {
public:
	EscherFunc() {
		beta = 0.30f;
		initParameterNames(1, "beta");
	}

	const char* getName() const {
		return "escher";
	}

	void setParameter(char *pName, JWF_FLOAT pValue) {
		if (strcmp(pName, "beta") == 0) {
			beta = pValue;
		}
	}

	void transform(FlameTransformationContext *pContext, XForm *pXForm, XYZPoint *pAffineTP, XYZPoint *pVarTP, JWF_FLOAT pAmount) {
		float a = pAffineTP->getPrecalcAtanYX();
		float lnr = 0.5f * JWF_LOG(pAffineTP->getPrecalcSumsq());

		float seb = JWF_SIN(beta);
		float ceb = JWF_COS(beta);

		float vc = 0.5f * (1.0f + ceb);
		float vd = 0.5f * seb;

		float m = pAmount * JWF_EXP(vc * lnr - vd * a);
		float n = vc * a + vd * lnr;

		float sn = JWF_SIN(n);
		float cn = JWF_COS(n);

		pVarTP->x += m * cn;
		pVarTP->y += m * sn;
		if (pContext->isPreserveZCoordinate) {
			pVarTP->z += pAmount * pAffineTP->z;
		}
	}

	EscherFunc* makeCopy() {
		return new EscherFunc(*this);
	}

private:
	float beta;
};

#endif // JWFVAR_ESCHER_H_
