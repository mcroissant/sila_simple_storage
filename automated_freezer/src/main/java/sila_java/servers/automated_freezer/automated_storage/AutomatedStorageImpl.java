package sila_java.servers.automated_freezer.automated_storage;

import sila2.org.silastandard.examples.automatedstorage.v1.AutomatedStorageGrpc;
import sila2.org.silastandard.examples.automatedstorage.v1.AutomatedStorageOuterClass;
import sila_java.library.core.sila.types.SiLAErrors;

public class AutomatedStorageImpl extends AutomatedStorageGrpc.AutomatedStorageImplBase{

    /**
     */
    @Override
    public void storeRackWithNoContentCheck(sila2.org.silastandard.examples.automatedstorage.v1.AutomatedStorageOuterClass.StoreRackWithNoContentCheck_Parameters request,
                                            io.grpc.stub.StreamObserver<sila2.org.silastandard.examples.automatedstorage.v1.AutomatedStorageOuterClass.StoreRackWithNoContentCheck_Responses> responseObserver) {


        /*
        Different parameters can be checked, it is mandatory to throw Validation Errors in case of
        missing parameters, which will be done automatically in the future.
        */
        if (!request.hasRackBarcode()) {
            responseObserver.onError(SiLAErrors.generateValidationError(
                    "Rack Barcode",
                    "Rack barcode parameter was not set",
                    "Specify a barcode with at least one character"));
            return;
        }

        /**
         * TODO implement any behaviour needed on the server side for the equipment
         */

        AutomatedStorageOuterClass.StoreRackWithNoContentCheck_Responses result =
                AutomatedStorageOuterClass.StoreRackWithNoContentCheck_Responses
                        .newBuilder()
                        .build();

        responseObserver.onNext(result);
        responseObserver.onCompleted();

    }
}
