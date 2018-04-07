package com.test.machineinfo.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by yuanaihong on 2018/3/22.
 */

public class HardwareInfoUtil {

    public static String getCpuInfo(String separator) {
        String cpuName;
        String processor = null;
        String BogoMIPS = null;
        String Features = null;
        String model_name = null;
        String CPU_implementer = null;
        String CPU_architecture = null;
        String CPU_variant = null;
        String CPU_part = null;
        String CPU_revision = null;
        String Hardware = null;

        try {
            String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
            ProcessBuilder cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            String readLine;
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));

            while ((readLine = responseReader.readLine()) != null) {
                String[] splitResult = readLine.split("\t:");

                if (processor == null && splitResult[0].toLowerCase().equals("processor")) {
                    processor = splitResult[1];
                } else if (BogoMIPS == null && splitResult[0].equals("BogoMIPS")) {
                    BogoMIPS = splitResult[1];
                } else if (Features == null && splitResult[0].toLowerCase().equals("features")) {
                    Features = splitResult[1];
                } else if (model_name == null && splitResult[0].equals("model name")) {
                    model_name = splitResult[1];
                } else if (CPU_implementer == null && splitResult[0].equals("CPU implementer")) {
                    CPU_implementer = splitResult[1];
                } else if (CPU_architecture == null && splitResult[0].equals("CPU architecture")) {
                    CPU_architecture = splitResult[1];
                } else if (CPU_variant == null && splitResult[0].equals("CPU variant")) {
                    CPU_variant = splitResult[1];
                } else if (CPU_part == null && splitResult[0].equals("CPU part")) {
                    CPU_part = splitResult[1];
                } else if (CPU_revision == null && splitResult[0].equals("CPU revision")) {
                    CPU_revision = splitResult[1];
                } else if (Hardware == null && splitResult[0].toLowerCase().equals("hardware")) {
                    Hardware = splitResult[1];
                }
            }

            responseReader.close();
        } catch (Exception e) {
            Log.e("TEST_INFO", "getCpuName: ", e);
        }

        cpuName = (processor == null ? "" : processor) + separator +
                (BogoMIPS == null ? "" : BogoMIPS) + separator +
                (Features == null ? "" : Features) + separator +
                (model_name == null ? "" : model_name) + separator +
                (CPU_implementer == null ? "" : CPU_implementer) + separator +
                (CPU_architecture == null ? "" : CPU_architecture) + separator +
                (CPU_variant == null ? "" : CPU_variant) + separator +
                (CPU_part == null ? "" : CPU_part) + separator +
                (CPU_revision == null ? "" : CPU_revision) + separator +
                (Hardware == null ? "" : Hardware);

        return cpuName;
    }

    /**
     * The default return value of any method in this class when an
     * error occurs or when processing fails (Currently set to -1). Use this to check if
     * the information about the device in question was successfully obtained.
     */
    public static final int DEVICEINFO_UNKNOWN = -1;

    /**
     * Reads the number of CPU cores from {@code /sys/devices/system/cpu/}.
     *
     * @return Number of CPU cores in the phone, or DEVICEINFO_UKNOWN = -1 in the event of an error.
     */
    public static int getNumberOfCPUCores() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            // Gingerbread doesn't support giving a single application access to both cores, but a
            // handful of devices (Atrix 4G and Droid X2 for example) were released with a dual-core
            // chipset and Gingerbread; that can let an app in the background run without impacting
            // the foreground application. But for our purposes, it makes them single core.
            return 1;
        }
        int cores;
        try {
            cores = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER).length;
        } catch (SecurityException e) {
            cores = DEVICEINFO_UNKNOWN;
        } catch (NullPointerException e) {
            cores = DEVICEINFO_UNKNOWN;
        }
        return cores;
    }

    private static final FileFilter CPU_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String path = pathname.getName();
            //regex is slow, so checking char by char.
            if (path.startsWith("cpu")) {
                for (int i = 3; i < path.length(); i++) {
                    if (path.charAt(i) < '0' || path.charAt(i) > '9') {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    };


    private final static String kCpuInfoMaxFreqFilePath = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";

    public static int getMaxCpuFreq() {
        int result = 0;
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(kCpuInfoMaxFreqFilePath);
            br = new BufferedReader(fr);
            String text = br.readLine();
            result = Integer.parseInt(text.trim());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fr != null)
                try {
                    fr.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }

        return result;
    }

    private final static String kCpuInfoMinFreqFilePath = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq";

    /* 获取CPU最小频率（单位KHZ） */
    public static int getMinCpuFreq() {
        int result = 0;
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(kCpuInfoMinFreqFilePath);
            br = new BufferedReader(fr);
            String text = br.readLine();
            result = Integer.parseInt(text.trim());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fr != null)
                try {
                    fr.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return result;
    }

    public static String getCPU_ABI(String separator) {

        try {
            StringBuffer buffer = new StringBuffer();

            if (Build.VERSION.SDK_INT >= 21) {
                String[] ABIs_surported = Build.SUPPORTED_ABIS;

                if (ABIs_surported.length > 0) {
                    for (int i = 0; i < ABIs_surported.length; i++) {
                        buffer.append(ABIs_surported[i]);
                        if (i < ABIs_surported.length - 1) {
                            buffer.append(separator);
                        }
                    }
                }

            } else {
                String abi = Build.CPU_ABI;
                String abi2 = Build.CPU_ABI2;

                buffer.append(abi);
                buffer.append(separator);
                buffer.append(abi2);
            }

            return buffer.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static int availableMemory(Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
//            return Formatter.formatFileSize(context, memoryInfo.availMem);
            return (int) (memoryInfo.availMem / 1024L / 1024L);
        } catch (Exception e) {
            return -1;
        }
    }

    public static int totalMemory(Context context) {
        if (Build.VERSION.SDK_INT >= 16) {
            try {
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                activityManager.getMemoryInfo(memoryInfo);

                return (int) (memoryInfo.totalMem / 1024L / 1024L);
            } catch (Exception e) {
            }
        }

        return -1;
    }

    public static String[] readRAMFromFile() {
        String path = "/proc/meminfo";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        int num = 3;
        int foundCount = 0;
        String[] ramList = new String[num];
        try {
            fis = new FileInputStream(new File(path));
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null && foundCount < num) {
                if (line.indexOf("MemTotal") > -1) {
                    ramList[0] = getRamValue(line);
                    foundCount++;
                } else if (line.indexOf("MemFree") > -1) {
                    ramList[1] = getRamValue(line);
                    foundCount++;
                } else if (line.indexOf("MemAvailable") > -1) {
                    ramList[2] = getRamValue(line);
                    foundCount++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                // just ignore
                e.printStackTrace();
            }
        }
        return ramList;
    }

    @NonNull
    private static String getRamValue(String line) {
        String result;
        int start = line.indexOf(":") + 1;
        int end = line.indexOf("kB");
        result = line.substring(start,
                end);
        //去空格
        result = result.trim();
        long sizeKb = Long.parseLong(result);
        result = (sizeKb / 1024) + "MB";
        return result;
    }
}
