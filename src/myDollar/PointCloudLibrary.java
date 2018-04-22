package myDollar;
/**
 * The $P Point-Cloud Recognizer (Java version)
 *
 *  by David White
 *  Copyright (c) 2012, David White. All rights reserved.
 *
 *  based entirely on the $P Point-Cloud Recognizer (Javascript version)
 *  found at http://depts.washington.edu/aimgroup/proj/dollar/pdollar.html
 *  who's original header follows:
 *
 *************************************************************************
 * The $P Point-Cloud Recognizer (JavaScript version)
 *
 *  Radu-Daniel Vatavu, Ph.D.
 *  University Stefan cel Mare of Suceava
 *  Suceava 720229, Romania
 *  vatavu@eed.usv.ro
 *
 *  Lisa Anthony, Ph.D.
 *      UMBC
 *      Information Systems Department
 *      1000 Hilltop Circle
 *      Baltimore, MD 21250
 *      lanthony@umbc.edu
 *
 *  Jacob O. Wobbrock, Ph.D.
 *  The Information School
 *  University of Washington
 *  Seattle, WA 98195-2840
 *  wobbrock@uw.edu
 *
 * The academic publication for the $P recognizer, and what should be
 * used to cite it, is:
 *
 *  Vatavu, R.-D., Anthony, L. and Wobbrock, J.O. (2012).
 *    Gestures as point clouds: A $P recognizer for user interface
 *    prototypes. Proceedings of the ACM Int'l Conference on
 *    Multimodal Interfaces (ICMI '12). Santa Monica, California
 *    (October 22-26, 2012). New York: ACM Press, pp. 273-280.
 *
 * This software is distributed under the "New BSD License" agreement:
 *
 * Copyright (c) 2012, Radu-Daniel Vatavu, Lisa Anthony, and
 * Jacob O. Wobbrock. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of the University Stefan cel Mare of Suceava,
 *  University of Washington, nor UMBC, nor the names of its contributors
 *  may be used to endorse or promote products derived from this software
 *  without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Radu-Daniel Vatavu OR Lisa Anthony
 * OR Jacob O. Wobbrock BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
**/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class PointCloudLibrary
{

  private static double CLOSE_ENOUGH = 0.3;
  private static PointCloudLibrary demoLibrary = null;
  private static String addrTemp;
  private ArrayList<PointCloud> _pointClouds =  new ArrayList<PointCloud>();

  public PointCloudLibrary()
  {
  }

  public PointCloudLibrary(ArrayList<PointCloud> pointClouds)
  {
    if(null == pointClouds)
    {
      throw new IllegalArgumentException("Point clouds cannot be null");
    }
    
    _pointClouds = pointClouds;
  }

  // the following is NOT part of the originally published javascript implementation
  // and has been added to support addition of directional testing for point clouds
  // which represent unistroke gestures
  public boolean containsOnlyUnistrokes()
  {
    for(int i = 0; i < _pointClouds.size(); i++)
    {
      if(!_pointClouds.get(i).isUnistroke())
      {
        return false;
      }
    }
    
    return true;
  }

  Set<PointCloud> getPointCloud(String name)
  {
    HashSet<PointCloud> result = new HashSet<PointCloud>();
    
    for(int i = 0; i < _pointClouds.size(); i++)
    {
      if(_pointClouds.get(i).getName().equals(name))
      {
        result.add(_pointClouds.get(i));
      }
    }
    
    return result;
  }
  
  public Set<String> getNames()
  {
    HashSet<String> result = new HashSet<String>();
    for(int i = 0; i < _pointClouds.size(); i++)
    {
      result.add(_pointClouds.get(i).getName());
    }
    
    return result;
  }
  
  public void addPointCloud(PointCloud cloud)
  {
    _pointClouds.add(cloud);
  }
  
  // removes one or more point clouds carrying the specified name (which
  // is case sensitive) from the library. if no matches are found, null is
  // returned, else those removed are returned
  public ArrayList<PointCloud> removePointCloud(String name)
  {
    ArrayList<PointCloud> result = null;
    
    if(null == name || "" == name)
    {
      throw new IllegalArgumentException("Name must be provided");
    }
    
    for(int i = 0; i < _pointClouds.size(); i++)
    {
      PointCloud p = _pointClouds.get(i);
      if(name != p.getName())
      {
        continue;
      }
      
      if(result == null)
      {
        result = new ArrayList<PointCloud>();
        result.add(p);
        _pointClouds.remove(i);
      }
    }
    
    return result;
  }
  
  public void clear()
  {
    if(this == demoLibrary)
    {
      _pointClouds = new ArrayList<PointCloud>();
      populateDemoLibrary(this);
    }
    else
    {
      _pointClouds = new ArrayList<PointCloud>();
    }
  }
  
  public int getSize()
  {
    return _pointClouds.size();
  }

  // most closely matches published javascript implementation of $P,
  // returns only the single, best match. note that the score member of
  // the result contains the aggregate distances between the two gestures.
  // for the original implementation see originalRecognize() below.
  public PointCloudMatchResult recognize(PointCloud inputGesture)
  {
    return recognize(inputGesture, false);
  }

  // as with published javascript implementation of $P, this returns only
  // the single, best match. however it permits use of directional testing
  // and, as such, should be used only with unistrokes. note that the score
  // member of the result contains the aggregate distances between the two
  // gestures. for the original implementation see originalRecognize() below.
  public PointCloudMatchResult recognize(PointCloud inputGesture, boolean testDirectionality)
  {
    return recognizeAll(inputGesture, testDirectionality)[0];
  }

  // unlike the published javascript implementation of $P, this returns an array
  // of results - one for each point cloud in the library sorted in order of
  // increasing aggregate distance between the point clouds. it also permits use of
  // some simple directional testing which should be used only with unistrokes. note
  // that the score member of the result contains the aggregate distance between the
  // two gestures. for the original implementation see originalRecognize() below.
  public PointCloudMatchResult[] recognizeAll(PointCloud inputGesture, boolean testDirectionality)
  {
    // the following is NOT part of the originally published javascript implementation
    // and has been added to support addition of directional testing for point clouds
    // which represent unistroke gestures
    if(testDirectionality)
    {
      if(!inputGesture.isUnistroke())
      {
        throw new IllegalArgumentException("If testDirectionality is true, input gesture must contain a unistroke");
      }

      if(! containsOnlyUnistrokes())
      {
        throw new IllegalArgumentException("If testDirectionality is true, the point cloud library must contain only unistroke point clouds");
      }
    }

    double b = Double.POSITIVE_INFINITY;
    int u = -1;
    PointCloudMatchResult[] results = new PointCloudMatchResult[_pointClouds.size()];

    for(int i = 0; i < _pointClouds.size(); i++) // for each point-cloud template
    {
      PointCloud pointCloud = _pointClouds.get(i);

      // the following is NOT part of the originally published javascript implementation
      // and has been added to support addition of directional testing for point clouds
      // which represent unistroke gestures
      if(testDirectionality)
      {
        // test to see if the gestures match roughly in directionality
        // if not, keep looking
        PointCloudPoint refStart = pointCloud.getFirstPoint();
        PointCloudPoint inStart = inputGesture.getFirstPoint();
        PointCloudPoint refEnd = pointCloud.getLastPoint();
        PointCloudPoint inEnd = inputGesture.getLastPoint();
        
        if((PointCloudUtils.distance(refStart, inStart) > CLOSE_ENOUGH) ||
           (PointCloudUtils.distance(refEnd, inEnd) > CLOSE_ENOUGH))
        {
          results[i] = new PointCloudMatchResult(pointCloud.getName(), Double.POSITIVE_INFINITY);
          continue;
        }
      }

      double d = pointCloud.greedyMatch(inputGesture);
      results[i] = new PointCloudMatchResult(pointCloud.getName(), d);
    }

    Arrays.sort(results, new Comparator<PointCloudMatchResult>()
    {
       public int compare(PointCloudMatchResult obj1, PointCloudMatchResult obj2)
        {
          if(obj1.getScore() < obj2.getScore())
          {
            return -1;
          }
  
          if(obj1.getScore() > obj2.getScore())
          {
            return 1;
          }
  
          return 0;
        }
    });

    return results;
  }
  
  /* this method implements the recognize routine as originally published in
   * javascript. in this author's experience:
  
   * (a) the score being normalized between 0 and 1 offered little meaning or
   * relationship to the quality of the match as implied by the word "score".
   * "correct" matches were found to result with scores at both extremes of the
   * range.
   * 
   * (b) on occasion it was helpful to have the results of matches for each of
   * the point clouds in the library. the original implementation provides only
   * the "best" match.
   * 
   * (c) $P as published implements directional invariance and does so by design.
   * the main rationale for this decision stems from the value of directional invariance
   * when the recognizer is used with multi-stroke gestures. however, this attribute
   * of the recognizer renders it incapable of discerning similar but semantically
   * different unistroke gestures such as a single top->bottom or left->right
   * from a single bottom->top or right->left. 
   */
  public PointCloudMatchResult originalRecognize(PointCloud inputGesture)
  {
    double b = Double.POSITIVE_INFINITY;
    int u = -1;

    for(int i = 0; i < _pointClouds.size(); i++) // for each point-cloud template
    {
      PointCloud pointCloud = _pointClouds.get(i);
      double d = inputGesture.greedyMatch(pointCloud);
      
      if (d < b)
      {
        b = d; // best (least) distance
        u = i; // point-cloud
      }
    }

    if(u == -1)
    {
      return new PointCloudMatchResult("No match", 0.0);
    }
    else
    {
      double r = Math.max((b - 2.0) / -2.0, 0.0);
      return new PointCloudMatchResult(_pointClouds.get(u).getName(), r);
    }
  }
  
  public static PointCloudLibrary getDemoLibrary()
  {
    if(null != demoLibrary)
    {
      return demoLibrary;
    }

    demoLibrary = new PointCloudLibrary();
    populateDemoLibrary(demoLibrary);
    return demoLibrary;
  }
  //CHANGES MADE HERE ********************************
  
  private static void populateDemoLibrary(PointCloudLibrary library)
  {
    final String dir = System.getProperty("user.dir");
    demoLibrary.addrTemp = dir+"\\gestureFiles\\";
    int k=1;
    
    ArrayList<PointCloudPoint> points = new ArrayList<PointCloudPoint>();
    try 
    {
      File directory = new File(demoLibrary.addrTemp); //for all files in the directory
      File[] allFiles = directory.listFiles();
      //loop
      for (File f1 : allFiles) 
      {
    	k=1;
        FileReader fr = new FileReader(demoLibrary.addrTemp+f1.getName());
        BufferedReader br = new BufferedReader(fr);
        String id = br.readLine();
        //System.out.println(name);
        String stream = br.readLine();
        								//for every gesture coordinates
        while(stream != null) 
        {
          if(stream.equals("BEGIN")) {
            stream = br.readLine();
            continue;
          }

          if (stream.equals("END")) {
            stream = br.readLine();
            k++;
            continue;
          }
          
          String[] st_var=stream.split(",");
          //System.out.println(Double.parseDouble(st_var[0])+" "+Double.parseDouble(st_var[1])+" "+id);
          points.add(new PointCloudPoint(Double.parseDouble(st_var[0]),Double.parseDouble(st_var[1]),k));
          stream = br.readLine();

        }

        library.addPointCloud(new PointCloud(id, points));
        br.close();
      }
      

    }
    catch(Exception e) {
    
      System.out.println(e.getStackTrace());
    }

//**ends here
  }
}
