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
package uk.ac.manchester.cs.diasmc.querycompiler.metadata.costmodels;

import java.io.IOException;

public class TempRadioCosts {

	static private final double transmitDuration = 16; 
	
	static private final double receiveDuration = 30;
	//221184; 

	/**
	 *  Gets the cost of sending a packet.
	 * 
	 * This is the difference between having the radio on anyway and sending the packet.
	 * 
	 * At low TxPowerSetting this can produce a negative number.
	 * 
	 * @param TxPowerSetting Power setting for the radio.
	 * @return margin energy cost.
	 */
	static public final double getMarginalSendpacketCost(int TxPowerSetting){
		return transmitDuration * (AvroraCostParameters.getTXAmpere(TxPowerSetting) -
			AvroraCostParameters.getRadioReceiveAmpere());
	}

	/**
	 * Gets the cost of having the radio on to receive a packet.
	 * 
	 * @return margin energy cost.
	 */
	static public final double getReceiveCost(){
		return receiveDuration * AvroraCostParameters.getRadioReceiveAmpere();
	}
	
	/**
	 * Gets the cost of transmitting a packet.
	 * Includes both the cost of having the radio on (in receive mode) 
	 * and the margin cost of sending a packet.
	 * 
	 * @param TxPowerSetting Power setting for the radio.
	 * @return margin energy cost.
	 */
	static public final double getTransmitCost(int TxPowerSetting) {
		return getMarginalSendpacketCost(TxPowerSetting) + getReceiveCost(); 
	}

	/**
	 * Testing Method
	 */
	public static void main(final String[] args) throws IOException {
		System.out.println(getReceiveCost());
		System.out.println(getMarginalSendpacketCost(15));
		System.out.println(getMarginalSendpacketCost(20));
		System.out.println(getMarginalSendpacketCost(225));
	}

}
