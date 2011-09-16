/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gagravarr.tika;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.gagravarr.flac.FlacOggPacket;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.vorbis.VorbisPacket;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Parser for non audio OGG files
 */
public class OggParser extends AbstractParser {
   private static List<MediaType> TYPES = Arrays.asList(new MediaType[] {
         MediaType.video("ogg"),
         MediaType.application("ogg"),
   });
   
   public Set<MediaType> getSupportedTypes(ParseContext context) {
      return new HashSet<MediaType>(TYPES);
   }
   
   public void parse(
         InputStream stream, ContentHandler handler,
         Metadata metadata, ParseContext context)
         throws IOException, TikaException, SAXException {
      OggFile ogg = new OggFile(stream);
      
      // For tracking
      int streams = 0;
      int flacCount = 0;
      int vorbisCount = 0;
      List<Integer> sids = new ArrayList<Integer>();
      
      
      // Check the streams in turn
      OggPacketReader r = ogg.getPacketReader();
      OggPacket p;
      while( (p = r.getNextPacket()) != null ) {
         if(p.isBeginningOfStream()) {
            streams++;
            sids.add(p.getSid());
            
            if(p.getData() != null && p.getData().length > 10) {
               if(VorbisPacket.isVorbisStream(p)) {
                  // Vorbis Audio stream
                  vorbisCount++;
               }
               if(FlacOggPacket.isFlacStream(p)) {
                  // FLAC-in-Ogg Audio stream
                  flacCount++;
               }
            }
         }
      }
      
      // Can we specialise?
      if(vorbisCount == 1 && flacCount == 0) {
         // TODO Pass to VorbisParser
      } else if(flacCount == 1 && vorbisCount == 0) {
         // TODO Pass to FlacParser
      } else if(streams > 0) {
         // TODO Handle each one in turn or something
      }
   }
}
