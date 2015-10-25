#Import libraries for doing image analysis
from skimage.io import imread
from skimage.transform import resize
from sklearn.ensemble import RandomForestClassifier as RF
import glob
import os
from sklearn import cross_validation
from sklearn.cross_validation import StratifiedKFold as KFold
from sklearn.metrics import classification_report
from matplotlib import pyplot as plt
from matplotlib import colors
from pylab import cm
from skimage import segmentation
from skimage.morphology import watershed
from skimage import measure
from skimage import morphology
import numpy as np
import pandas as pd
import cv2
from scipy import ndimage
from skimage.feature import peak_local_max


import warnings
warnings.filterwarnings("ignore")
# get the classnames from the directory structure
directory_names = list(set(glob.glob(os.path.join("competition_data","train", "*"))\
 ).difference(set(glob.glob(os.path.join("competition_data","train","*.*")))))

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

#merge the key point together
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

#get the total training images
numberofImages = 0
for folder in directory_names:
    for fileNameDir in os.walk(folder):
        for fileName in fileNameDir[2]:
             # Only read in the images
            if fileName[-4:] != ".jpg":
              continue
            numberofImages += 1

# We'll rescale the images to be 25x25
maxPixel = 25
imageSize = maxPixel * maxPixel
num_rows = numberofImages # one row for each image in the training dataset
num_features = imageSize + 1 # for our ratio

# X is the feature vector with one row of features per image
# consisting of the pixel values and our metric
X = np.zeros((num_rows, num_features + 128 * 3), dtype=float)
# y is the numeric class label
y = np.zeros((num_rows))

files = []
# Generate training data
i = 0
label = 0
# List of string of class names
namesClasses = list()

print "Reading images"

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

            img = cv2.imread(nameFileImage)
            gray= cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
            sift = cv2.SIFT()
            kp, des = sift.detectAndCompute(gray,None)
            pnts = mergeKeyPoints(des)
            X[i, imageSize + 1 : imageSize + 128 * 3 + 1] = pnts
            # Store the classlabel
            y[i] = label
            i += 1
            # report progress for each 5% done
            report = [int((j+1)*num_rows/20.) for j in range(20)]
            if i in report: print np.ceil(i *100.0 / num_rows), "% done"
    label += 1
    
print "Training"
# n_estimators is the number of decision trees
# max_features also known as m_try is set to the default value of the square root of the number of features
clf = RF(n_estimators=100, n_jobs=3);
scores = cross_validation.cross_val_score(clf, X, y, cv=5, n_jobs=1);
print "Accuracy of all classes"
print np.mean(scores)