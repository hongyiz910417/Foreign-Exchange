
# Experiment Analytics
                Hongyi Zhang(andrew id: hongyiz)
##Basic Solution
###Goal
To develop a machine learning model based on the given training set which can classify a plankton image.
###libraries used
scikit-learn, scikit-image, pandas
###Procedure overview
For the first version of my solution, I learnt from the tutorials provided by data science bowl. 
The machine learning model that I choose is Random Forrest. 
The features are the following: 
1. For each image, I first eliminate all the noise parts(in another word, empty part), and then calculate the ratio of the width of the meaningful part and the length of the meaningful part, and use this ratio as a feature. The reason I choose this ratio as a feature is that for different types of plankton creatures, there is a great possibility that their width/length ratio is different, so this feature can be useful in classifying a picture.
2. For each image, I rescale the image into 25 * 25 size. In this way, each image has 625 pixels. I define each pixel as a feature for the image. Thus I got 625 more features.
###detailed steps
1.data preparation
The followings are the code for extracting the 626 features for each image.



```python
# Navigate through the list of directories
for folder in directory_names:
    # Append the string class name for each class
    currentClass = folder.split(os.pathsep)[-1]
    namesClasses.append(currentClass)
    for fileNameDir in os.walk(folder):
        for fileName in fileNameDir[2]:
            # Only read in the images
            if fileName[-4:] != ".jpg":
              continue

            # Read in the images and create the features
            nameFileImage = "{0}{1}{2}".format(fileNameDir[0], os.sep, fileName)
            image = imread(nameFileImage, as_grey=True)
            files.append(nameFileImage)
            axisratio = getMinorMajorRatio(image)
            image = resize(image, (maxPixel, maxPixel))

            # Store the rescaled image pixels and the axis ratio
            X[i, 0:imageSize] = np.reshape(image, (1, imageSize))
            X[i, imageSize] = axisratio
            # Store the classlabel
            y[i] = label
            i += 1
            # report progress for each 5% done
            report = [int((j+1)*num_rows/20.) for j in range(20)]
            if i in report: print np.ceil(i *100.0 / num_rows), "% done"
    label += 1
```

The following is the code for extracting width/length ratio feature(which is called in the above data preparing step).


```python
# find the largest nonzero region
def getLargestRegion(props, labelmap, imagethres):
    regionmaxprop = None
    for regionprop in props:
        # check to see if the region is at least 50% nonzero
        if sum(imagethres[labelmap == regionprop.label])*1.0/regionprop.area < 0.50:
            continue
        if regionmaxprop is None:
            regionmaxprop = regionprop
        if regionmaxprop.filled_area < regionprop.filled_area:
            regionmaxprop = regionprop
    return regionmaxprop



def getMinorMajorRatio(image):
    image = image.copy()
    # Create the thresholded image to eliminate some of the background
    imagethr = np.where(image > np.mean(image),0.,1.0)

    #Dilate the image
    imdilated = morphology.dilation(imagethr, np.ones((4,4)))

    # Create the label list
    label_list = measure.label(imdilated)
    label_list = imagethr*label_list
    label_list = label_list.astype(int)

    region_list = measure.regionprops(label_list)
    maxregion = getLargestRegion(region_list, label_list, imagethr)

    # guard against cases where the segmentation fails by providing zeros
    ratio = 0.0
    if ((not maxregion is None) and  (maxregion.major_axis_length != 0.0)):
        ratio = 0.0 if maxregion is None else  maxregion.minor_axis_length*1.0 / maxregion.major_axis_length
    return ratio
```

2. build random forest with prepared data and cross-validate it(using scikit-learn).


```python
print "Training"
# n_estimators is the number of decision trees
# max_features also known as m_try is set to the default value of the square root of the number of features
clf = RF(n_estimators=100, n_jobs=3);
scores = cross_validation.cross_val_score(clf, X, y, cv=5, n_jobs=1);
print "Accuracy of all classes"
print np.mean(scores)
```

###Performance
The correctness is 0.46652095171
###Analytics
Since we have as much as 40 classes to classify, 0.46 is an acceptable score for the first version.
However, for the features I choose, there exists some weakness.
First, it is possible that the same type of plankton images is rotated, causing the width/length ratio is not the same.
Second, we have 625 features which simply use the raw pixel bit information. There must be something we can do to make more use of those information.
##Improved Solution
###Goal
To concur the weakness of directly using 625 pixels without any processing steps.
###Libraries used
opencv
###Procedure overview
To get more information out of the image pixels, I decided to use SIFT package provided in OpenCV.
SIFT provides a function which can detect the keypoints of an image which can represent the image for matching between images. So I used this function to generate a list of keypoints for each image. Each keypoint is consisted of 128 floats. So for image I added 128 more features, the value of each feature is the sum of the corresponding float in each keypoint from this image.
###Detailed Steps
1. The following code is inserted into the process of the previous preparing data step, in order to extract the keypoints for each image.


```python
img = cv2.imread(nameFileImage)
gray= cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
sift = cv2.SIFT()
kp, des = sift.detectAndCompute(gray,None)
pnts = mergeKeyPoints(des)
X[i, imageSize + 1 : imageSize + 129] = pnts
```

2.The following code is for merging all the keypoints for this image into an array of 128 features.


```python
def mergeKeyPoints(des):
	pnts = np.zeros(128, dtype=float)
	if des == None : return pnts
	l = des.size / 128
	for i in range(0, l): 
        pnts = pnts + des[i]
	return pnts
```

###Performance
The correctness rate is 0.493774219102
###Analytics
The result shows that applying keypoints as features has a positive effect. However, simply summing up all the corresponding digits in each keypoints is a method that can be improved.
##Improved Solution2(failed)
###Goal
To improve the way I deal with keypoints.
##Procedure Overview
Instead of makeing 128 features storing all the sum of all key points, I decided to store 3 keypoints(with the total of 128*3 features). For the generation of the 3 keypoints, I used K-means algorithm. First I will conduct K-means on the set of keypoints, and generate 3 center points. And then I will return the 3 center points as the 3 keypoints for my newly added 128*3 features.
##Detailed Steps
1. change the code in Improved Solution1  for preparing keypoint features as following.


```python
 # Store the rescaled image pixels and the axis ratio
X[i, 0:imageSize] = np.reshape(image, (1, imageSize))
X[i, imageSize] = axisratio

img = cv2.imread(nameFileImage)
gray= cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
sift = cv2.SIFT()
kp, des = sift.detectAndCompute(gray,None)
pnts = mergeKeyPoints(des)
X[i, imageSize + 1 : imageSize + 128 * 3 + 1] = pnts
```

2. change the code in Improve Solution1 for merging keypoints as following.


```python
def mergeKeyPoints(des):
	pnts = np.zeros(3 * 128, dtype=float)
	if des == None : return pnts
	l = des.size / 128
	if l >= 3:
		criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 10, 1.0)
		ret, labels, centers = cv2.kmeans(des, 3,criteria,10,cv2.KMEANS_RANDOM_CENTERS)
		for i in range(0, 127):
			pnts[i] =  centers[0][i]
			pnts[i + 128] = centers[1][i]
			pnts[i + 128 * 2] = centers[2][i]
	return pnts
```

###Performance
The correctness rate is: 0.45890362399
###Analytics
The result shows that the performance for this version is even worse than the original version. I think the reason is that for kmeans algorithm, the keypoints for each image is too small(At most around 20, some even has 0 keypoints). Thus the merged center points can not represent the whole image.
##Summary
After implementing and comparing the above 3 versions, it shows that the second version is the best, with 25*25 pixels, width/length and sum of kepoints as features.
