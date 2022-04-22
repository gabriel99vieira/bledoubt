# BLE-Doubt Analysis

This library contains the data analysis which forms the basis of our eponymous 2022 SafeThings Workshop Paper. The `bledoubt` package contains classes which manage trajectories and classifiers as well as some experimental trajectory fusion code which we did not include in the paper. The other top-level files perform data analysis tasks which were useful for our experiemnts. All our code is licensed to Jimmy Briggs and Chris Geeng with the MIT license provided in repository.

## Analyzing BLE-Doubt Logs

The `bledoubt_analysis.py` file generates the confusion matrices which compare our classifiers. The script is run like this:
```bash
python3 bledoubt_analysis.py -o <figure_output_directory> <log file directory>
```
`bledoubt_analysis.py` will successfully analyze all files in the log file directory which conform to the BLE-doubt schema and use the file extension `.json`. Do not include any other `.json` files in the log file directory, or the script will error. The one exception is that any file named `gt_macs.json` will be skipped. We use that name specifically for ground truth files that label MAC addresses as malicious or benign.

After `bledoubt_analysis.py` runs, a confusion matrix `.png` image should be available in the figure output directory.

Our anonymized dataset is available in the `logs.zip` archive.

## Working with a Collection of Logs

The `manage_dataset.py` script allows a number of operations. Chief among them is the ability to strip personally identifying information (PII), such as MAC addresses and device names from log files. When used in this capacity, `manage_dataset.py` should be run on the whole dataset (and any ground truth file) all at once. Otherwise problems will arise in MAC address aliasing. The syntax for anonymizing logs is as follows:

```bash
python3 manage_dataset.py anonymize <input directory> <output directory>
```

A number of plotting functionalities also exist in `manage_dataset.py` such as the ability to plot RSSI histograms over time, plot RSSI timing distributions over time, draw trajectories in 2D space, filter for loud devices, and more. These functionalities have not been optimized for useability. If you need them, you will either have to match our directory structure [it's easy; logs go in the `logs` directory] or mess with some string literals. Your mileage may vary.

