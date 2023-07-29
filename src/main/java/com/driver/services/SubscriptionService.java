package com.driver.services;


import com.driver.EntryDto.SubscriptionEntryDto;
import com.driver.model.Subscription;
import com.driver.model.SubscriptionType;
import com.driver.model.User;
import com.driver.repository.SubscriptionRepository;
import com.driver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SubscriptionService {

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    UserRepository userRepository;

    public Integer buySubscription(SubscriptionEntryDto subscriptionEntryDto){

        //Save The subscription Object into the Db and return the total Amount that user has to pay
        //convert dto->entity
        User user=userRepository.findById(subscriptionEntryDto.getUserId()).get();
        Subscription subscription=new Subscription();
        subscription.setSubscriptionType(subscriptionEntryDto.getSubscriptionType());
        subscription.setNoOfScreensSubscribed(subscriptionEntryDto.getNoOfScreensRequired());
        subscription.setStartSubscriptionDate(new Date());
        //For Basic Plan : 500 + 200noOfScreensSubscribed
        // For PRO Plan : 800 + 250noOfScreensSubscribed
        // For ELITE Plan : 1000 + 350*noOfScreensSubscribed
        int payment=0;
        SubscriptionType type=subscriptionEntryDto.getSubscriptionType();
        if(type.equals(SubscriptionType.BASIC))
            payment=500+200*subscription.getNoOfScreensSubscribed();
        else if(type.equals(SubscriptionType.PRO))
            payment=800+250*subscription.getNoOfScreensSubscribed();
        else if(type.equals(SubscriptionType.ELITE))
            payment=1000+350*subscription.getNoOfScreensSubscribed();
        subscription.setTotalAmountPaid(payment);
        subscription.setUser(user);
        Subscription savedSubscription=subscriptionRepository.save(subscription);
        //updating user class
        //since user is already a saved entity we dont need to dave it again
        user.setSubscription(savedSubscription);
        return savedSubscription.getTotalAmountPaid();



    }

    public Integer upgradeSubscription(Integer userId)throws Exception{

        //If you are already at an ElITE subscription : then throw Exception ("Already the best Subscription")
        //In all other cases just try to upgrade the subscription and tell the difference of price that user has to pay
        //update the subscription in the repository
        User user=userRepository.findById(userId).get();
        Subscription subscription=user.getSubscription();
        if(subscription.getSubscriptionType().equals(SubscriptionType.ELITE))
            throw new Exception("Already the best Subscription");
        else if(subscription.getSubscriptionType().equals(SubscriptionType.BASIC)){
            int noOfScreens=subscription.getNoOfScreensSubscribed();
            int diffAmount=300+noOfScreens*50;
            subscription.setSubscriptionType(SubscriptionType.PRO);
            subscription.setTotalAmountPaid(subscription.getTotalAmountPaid()+diffAmount);
            subscriptionRepository.save(subscription);
            return diffAmount;

        }else if(subscription.getSubscriptionType().equals(SubscriptionType.PRO)){
            int noOfScreens=subscription.getNoOfScreensSubscribed();
            int diffAmount=200+noOfScreens*100;
            subscription.setSubscriptionType(SubscriptionType.ELITE);
            subscription.setTotalAmountPaid(subscription.getTotalAmountPaid()+diffAmount);
            subscriptionRepository.save(subscription);
            return diffAmount;
        }


        return null;
    }

    public Integer calculateTotalRevenueOfHotstar(){

        //We need to find out total Revenue of hotstar : from all the subscriptions combined
        //Hint is to use findAll function from the SubscriptionDb
        List<Subscription> list=subscriptionRepository.findAll();
        int revenue=0;
        for(Subscription ele:list){
            revenue+=ele.getTotalAmountPaid();
        }

        return revenue;
    }

}
