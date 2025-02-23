package com.example.dynamopush;

import android.content.Context;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

public class ONNXModelHelper {
    private OrtEnvironment ortEnvironment;
    private OrtSession ortSession;
    private MinMaxScalerHelper scaler;

    public ONNXModelHelper(Context context) {
        scaler = new MinMaxScalerHelper(8.0f, 17558.0f, -0.002f, 469.151f);
        loadModel(context);
    }

    private void loadModel(Context context) {
        try {
            ortEnvironment = OrtEnvironment.getEnvironment();
            InputStream inputStream = context.getAssets().open("optimized_random_forest_1.onnx");
            byte[] modelBytes = new byte[inputStream.available()];
            inputStream.read(modelBytes);
            inputStream.close();
            ortSession = ortEnvironment.createSession(modelBytes);
            System.out.println("✅ ONNX Session created successfully!");
        } catch (Exception e) {
            System.out.println("❌ Error loading ONNX model!");
            e.printStackTrace();
        }
    }

    public float predict(float inputValue) {
        try {
            if (ortSession == null) {
                System.out.println("❌ ONNX Session is null!");
                return -1f;
            }
            float normalizedInput = scaler.normalizeX(inputValue);
            System.out.println("✅ Normalized Input: " + normalizedInput);

            long[] inputShape = {1, 1};
            FloatBuffer floatBuffer = FloatBuffer.wrap(new float[]{normalizedInput});
            OnnxTensor inputTensor = OnnxTensor.createTensor(ortEnvironment, floatBuffer, inputShape);


            Map<String, OnnxTensor> inputs = Collections.singletonMap(ortSession.getInputNames().iterator().next(), inputTensor);
            OrtSession.Result result = ortSession.run(inputs);

            float[][] outputTensor = (float[][]) result.get(0).getValue();
            float rawPrediction = outputTensor[0][0];
            float finalPrediction = scaler.inverseTransformY(rawPrediction);

            System.out.println("✅ ONNX Prediction: " + finalPrediction);
            return finalPrediction;
        } catch (Exception e) {
            System.out.println("❌ Exception in ONNX Prediction");
            e.printStackTrace();
            return -1f;
        }
    }
}
