package org.Roclh.data.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.UserModel;
import org.Roclh.ss.ShadowsocksProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServerSharingService {
    private final ShadowsocksProperties shadowsocksProperties;

    @Nullable
    public String generateServerUrl(@NonNull UserModel userModel) {
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append("//").append(Base64.getEncoder().encodeToString((shadowsocksProperties.getDefaultMethod() + ":" + userModel.getPassword()).getBytes()));
        uriBuilder.append("@").append(shadowsocksProperties.getAddress()).append(":").append(userModel.getUsedPort());
        uriBuilder.append("/");
        try {
            uriBuilder = new StringBuilder(new URI("ss", uriBuilder.toString(), "").toString());
            uriBuilder.deleteCharAt(uriBuilder.length() - 1);
        } catch (URISyntaxException e) {
            log.error("Failed to create URI string", e);
        }
        if (!userModel.getPlugin().equals(UserModel.Plugin.DEFAULT)) {
            uriBuilder.append("?plugin=").append(userModel.getPlugin().getPluginLinkPostfix());
            if (!userModel.getPlugin().getPluginOpts().isEmpty()) {
                uriBuilder.append(userModel.getPlugin().getPluginOpts().entrySet().stream().map(entry ->
                        URLEncoder.encode(";" + entry.getKey() + "=" + entry.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("")));
            }
        }
        uriBuilder.append("#").append(userModel.getUserModel().getTelegramName()).append(":").append(userModel.getUserModel().getTelegramId());
        return uriBuilder.toString();
    }

    @Nullable
    public BufferedImage generateServerUrlQrCode(@NonNull UserModel userModel){
        String uri = generateServerUrl(userModel);
        if(uri == null){
            log.error("Generated URI is null!");
            return null;
        }
        try {
            return createQR(uri);
        } catch (WriterException e) {
            log.error("Failed to create QR code from URI {}", uri, e);
        }
        return null;
    }

    private BufferedImage createQR(@NonNull String data) throws WriterException {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix matrix = barcodeWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }
}
