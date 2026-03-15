package render;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

public class FrustumCulling {
    private final FrustumIntersection frustumInt;
    private final Matrix4f prjViewMatrix;

    public FrustumCulling() {
        frustumInt = new FrustumIntersection();
        prjViewMatrix = new Matrix4f();
    }

    public void update(Matrix4f projMatrix, Matrix4f viewMatrix) {
        prjViewMatrix.set(projMatrix).mul(viewMatrix);
        frustumInt.set(prjViewMatrix);
    }

    public boolean insideFrustum(float x, float y, float z, float radius) {
        return frustumInt.testSphere(x, y, z, radius);
    }
}
