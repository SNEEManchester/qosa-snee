/****************************************************************************\ 
*                                                                            *
*  SNEE (Sensor NEtwork Engine)                                              *
*  http://code.google.com/p/snee                                             *
*  Release 1.0, 24 May 2009, under New BSD License.                          *
*                                                                            *
*  Copyright (c) 2009, University of Manchester                              *
*  All rights reserved.                                                      *
*                                                                            *
*  Redistribution and use in source and binary forms, with or without        *
*  modification, are permitted provided that the following conditions are    *
*  met: Redistributions of source code must retain the above copyright       *
*  notice, this list of conditions and the following disclaimer.             *
*  Redistributions in binary form must reproduce the above copyright notice, *
*  this list of conditions and the following disclaimer in the documentation *
*  and/or other materials provided with the distribution.                    *
*  Neither the name of the University of Manchester nor the names of its     *
*  contributors may be used to endorse or promote products derived from this *
*  software without specific prior written permission.                       *
*                                                                            *
*  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS   *
*  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, *
*  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR    *
*  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR          *
*  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,     *
*  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,       *
*  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR        *
*  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
*  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING      *
*  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS        *
*  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.              *
*                                                                            *
\****************************************************************************/
package uk.ac.manchester.cs.diasmc.querycompiler.codeGeneration.adt;

import java.io.IOException;

import uk.ac.manchester.cs.diasmc.querycompiler.metadata.sensornet.Site;
import uk.ac.manchester.cs.diasmc.querycompiler.queryplan.Fragment;

public class AMRecieveComponent extends GenericNesCComponent implements
	TinyOS2Component {

    public static String TYPE_NAME = "AMReceiverC";

    Site txSite;

    Fragment sourceFrag;

    Fragment destFrag;

    Site destSite;

    public AMRecieveComponent(final Fragment sourceFrag,
	    final Site destSite, final Fragment destFrag, final Site txSite, final String name,
	    final NesCConfiguration config, final String activeMessageID,
	    boolean tossimFlag) {
    	
		super(config, TYPE_NAME, activeMessageID, 2, tossimFlag);
		this.sourceFrag = sourceFrag;
		this.destSite = destSite;
		this.destFrag = destFrag;
		this.txSite = txSite;
		this.id = this.generateName();
    }

    @Override
    public String toString() {
	return this.getID();
    }

    private String generateName() {
	return "AMRecieveCFrag" + this.sourceFrag.getID() + "Frag"
		+ this.destFrag.getID() + "n" + this.destSite.getID() + "_n"
		+ this.txSite.getID() + "P";
    }

    @Override
    public void writeNesCFile(final String outputDir)
	    throws IOException {
	//Do nothing!!
    }

}
